package SpringCommerce.project.controller;

import SpringCommerce.project.dto.CategoryDTO;
import SpringCommerce.project.dto.ProductDTO;
import SpringCommerce.project.dto.UserDTO;
import SpringCommerce.project.model.Category;
import SpringCommerce.project.model.Product;
import SpringCommerce.project.model.Role;
import SpringCommerce.project.model.User;
import SpringCommerce.project.service.CategoryService;
import SpringCommerce.project.service.ProductService;
import SpringCommerce.project.service.RoleService;
import SpringCommerce.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class AdminController {
    public static String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/productImages";
//    @Autowired
//    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/admin")
    public String adminHome(Model model){
        model.addAttribute("currenPage","management");
        return "adminHome";
    }//page admin home

    //Accounts
    @GetMapping("/admin/users")
    public String getAcc(Model model){
        model.addAttribute("users", userService.getAllUser());
        //model.addAttribute("roles", roleService.getAllRole());
        return "users";
    }
    @GetMapping("/admin/users/add")
    public String getUserAdd(Model model){
        model.addAttribute("userDTO", new UserDTO());
        model.addAttribute("roles",roleService.getAllRole());
        model.addAttribute("newUser","new");
        return "usersAdd";
    }
    @PostMapping("/admin/users/add")
    public String postUserAdd(@ModelAttribute("userDTO") UserDTO userDTO) {
        //convert dto > entity
        User user = new User();
        System.out.println(userDTO.getId());
        if (userDTO.getId() != null)
            user = userService.getUserById(userDTO.getId()).get();
        else user.setPassword((bCryptPasswordEncoder.encode("1")));
        user.setEmail(userDTO.getEmail());
//        user.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
//        user.setPassword(userDTO.getPassword());
        user.setName(userDTO.getName());
        user.setAddress(userDTO.getAddress());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        List<Role> roles = new ArrayList<>();
        for (Integer item: userDTO.getRoleIds()) {
            roles.add(roleService.findRoleById(item).get());
        }
        user.setRoles(roles);

        userService.updateUser(user);
        return "redirect:/admin/users";
    }
    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id){
        userService.removeUserById(id);
        return "redirect:/admin/users";
    }//delete 1 user
    @GetMapping("/admin/users/update/{id}")
    public String updateUser(@PathVariable Long id, Model model){
        User user = userService.getUserById(id).orElse(null);
        if (user != null){
            //convert entity > dto
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setPassword("");
            userDTO.setName(user.getName());
            userDTO.setAddress(user.getAddress());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            List<Integer> roleIds = new ArrayList<>();
            for (Role item:user.getRoles()) {
                roleIds.add(item.getId());
            }

            model.addAttribute("userDTO", userDTO);
            model.addAttribute("roles", roleService.getAllRole());
            return "usersAdd";
        }else {
            return "404";
        }

    }

    //Categories session
    @GetMapping("/admin/categories")
    public String getCat(Model model){
        model.addAttribute("categories", categoryService.getAllCategory());
        return "categories";
    }//view all categories
    @GetMapping("/admin/categories/add")
    public String getCatAdd(Model model){
        model.addAttribute("category", new Category());
        return "categoriesAdd";
    }//form add new category
    @PostMapping("/admin/categories/add")
    public String postCatAdd(@ModelAttribute("category") Category category){
        categoryService.updateCategory(category);
        return "redirect:/admin/categories";
    }//form add new category > do add
    @GetMapping("/admin/categories/delete/{id}")
    public String deleteCat(@PathVariable Long id){
        categoryService.removeCategoryById(id);
        return "redirect:/admin/categories";
    }//delete 1 category
    @GetMapping("/admin/categories/update/{id}")
    public String updateCat(@PathVariable Long id, Model model){
        Optional<Category> category = categoryService.getCategoryById(id);
        if(category.isPresent()){
            model.addAttribute("category", category.get());
            return "categoriesAdd";
        }else {
            return "404";
        }
    }//form edit category, fill old data into form

    //Products session
    @GetMapping("/admin/products")
    public String getPro(Model model){
        model.addAttribute("products", productService.getAllProduct());
        return "products";
    }//view all products
    @GetMapping("/admin/products/add")
    public String getProAdd(Model model){
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategory());
        return "productsAdd";
    }// form add new product
    @PostMapping("/admin/products/add")
    public String postProAdd(@ModelAttribute("productDTO") ProductDTO productDTO,
                             @RequestParam("productImage") MultipartFile fileProductImage,
                             @RequestParam("categories") String[] selectCategories,
                             @RequestParam("imgName") String imgName) throws IOException {
        System.out.println(selectCategories);
        Product product = new Product();
        if (productDTO.getId() != null) product = productService.getProductById(productDTO.getId()).get();

        //convert dto > entity
        product.setName(productDTO.getName());
        Set<Category> categories = new HashSet<>();
        for (String categoryId : selectCategories) {
            categories.add(categoryService.getCategoryById(Long.parseLong(categoryId)).get());
        }
        product.setCategories(categories);
        product.setPrice(productDTO.getPrice());
        product.setWeight(productDTO.getWeight());
        product.setDescription(productDTO.getDescription());
        product.setBrand(productDTO.getBrand());
        product.setColor(productDTO.getColor());
        String imageUUID;
        if(!fileProductImage.isEmpty()){
            imageUUID = fileProductImage.getOriginalFilename();
            Path fileNameAndPath = Paths.get(uploadDir, imageUUID);
            Files.write(fileNameAndPath, fileProductImage.getBytes());
        }else {
            imageUUID = imgName;
        }//save image
        product.setImageName(imageUUID);

        productService.addOrUpdateProduct(product);
        return "redirect:/admin/products";
    }//form add new product > do add
    @GetMapping("/admin/products/delete/{id}")
    public String deletePro(@PathVariable long id){
        productService.removeProductById(id);
        return "redirect:/admin/products";
    }//delete 1 product
    @GetMapping("/admin/products/update/{id}")
    public String updatePro(@PathVariable long id, Model model){
        Optional<Product> opProduct = productService.getProductById(id);
        if (opProduct.isPresent()){
            Product product = opProduct.get();
            //convert entity > dto
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());

            Set<CategoryDTO> categories = new HashSet<>();
            for (Category category : product.getCategories()) {
                CategoryDTO Categorydto = new CategoryDTO();
                Categorydto.setId(category.getId());
                Categorydto.setName(category.getName());
                categories.add(Categorydto);
            }

            productDTO.setCategories(categories);
            productDTO.setPrice(product.getPrice());
            productDTO.setWeight(product.getWeight());
            productDTO.setDescription(product.getDescription());
            productDTO.setImageName(product.getImageName());

            model.addAttribute("productDTO", productDTO);
            model.addAttribute("categories", categoryService.getAllCategory());
            return "productsAdd";
        }else {
            return "404";
        }

    }//form edit product, fill old data into form
}
