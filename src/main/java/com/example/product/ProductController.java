package com.example.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    private ProductDAO productDAO;


    @RequestMapping("/") // 시작페이지
    public String home() {
        return "redirect:/list";
    }

    @RequestMapping("/list")
    public ModelAndView list(@RequestParam(defaultValue = "") String product_name, ModelAndView mav) {
        mav.setViewName("list");
        mav.addObject("list", productDAO.list(product_name));
        mav.addObject("product_name", product_name);

        return mav;
    }

    @RequestMapping("/write")
    public String write() {
        return "write";
    }

    @RequestMapping("/insert")
    public String insert(@RequestParam Map<String, Object> map, @RequestParam MultipartFile img, HttpServletRequest request) {
        String filename = "-";
        if (img != null && !img.isEmpty()) { //첨부파일이 있으면
            filename = img.getOriginalFilename(); // 파일이름
            try {
                ServletContext application = request.getSession().getServletContext();
                //배포 디렉토리
                String path = application.getRealPath("/resources/images/");
                img.transferTo(new File(path + filename)); // 첨부파일 저장
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        map.put("filename", filename);
        productDAO.insert(map);
        return "redirect:/list";
    }

    @RequestMapping("/detail/{product_code}")
    public ModelAndView detail(@PathVariable String product_code, ModelAndView mav) {
        mav.setViewName("detail");
        mav.addObject("product", productDAO.detail(product_code));

        return mav;
    }

    @RequestMapping("/update")
    public String update(@RequestParam Map<String, Object> map, @RequestParam MultipartFile img, HttpServletRequest request) {
        String filename = "-";
        if (img != null && !img.isEmpty()) { // 첨부파일 있을 때
            filename = img.getOriginalFilename();
            try {
                ServletContext application = request.getSession().getServletContext();
                String path = application.getRealPath("/resources/images/");
                img.transferTo(new File(path + filename));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { // 첨부파일이 없을 때
            String product_code = map.get("product_code").toString();
            Map<String, Object> product = productDAO.detail(product_code);

            filename = product.get("filename").toString();

        }
        map.put("filename", filename);
        productDAO.update(map);
        return "redirect:/list";
    }

    @RequestMapping("/delete")
    public String delete(int product_code, HttpServletRequest request) {
        String filename = productDAO.filename(product_code);
        if (filename != null && !filename.equals("-")) { // 첨부파일 있으면
            ServletContext application = request.getSession().getServletContext();
            String path = application.getRealPath("/resources/images/");
            File file = new File(path + filename);
            if (file.exists()) { // 파일이 존재하면
                file.delete(); // 삭제
            }
        }
        productDAO.delete(product_code);
        return "redirect:/list";
    }
}
