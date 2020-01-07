package com.jeecg.p3.system.web;

import com.jeecg.p3.system.util.RandomCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Controller
@RequestMapping("/randomCode")
public class RandomCodeController {

    private static final Logger logger = LoggerFactory.getLogger(RandomCodeController.class);

    @RequestMapping(value = "", method = {RequestMethod.GET,RequestMethod.POST})
    public void get(HttpServletRequest request, HttpServletResponse response) {
        // response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expire", "0");
        response.setHeader("Pragma", "no-cache");

        RandomCodeUtil validateCode = new RandomCodeUtil();

        // 直接返回图片
        String code = validateCode.getRandomCodeImage(request, response);

        logger.info("validateCode:{}",code);
    }


}
