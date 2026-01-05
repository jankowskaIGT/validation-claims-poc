package com.scms.validation_claims.controller;


import com.scms.validation_claims.dto.CheckRequest;
import com.scms.validation_claims.dto.CheckResponse;
import com.scms.validation_claims.service.CheckClaimService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UiController {

    private final CheckClaimService service;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("checkRequest", new CheckRequest());
        return "index";
    }


    @PostMapping("/check")
    public String check(
            @ModelAttribute("checkRequest") @Valid CheckRequest checkRequest,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        CheckResponse response = service.check(checkRequest);
        model.addAttribute("result", response);
        return "index";
    }

}

