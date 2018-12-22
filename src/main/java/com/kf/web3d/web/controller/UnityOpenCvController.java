package com.kf.web3d.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by ye on 2018/12/22.
 */
@Controller
@RequestMapping("/unityOpenCv")
public class UnityOpenCvController {

    @RequestMapping("/index")
    public String index() {
        return "unityOpenCv/index";
    }
}
