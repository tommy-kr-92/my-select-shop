package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.SignupRequestDto;
import com.sparta.myselectshop.dto.UserInfoDto;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import com.sparta.myselectshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final FolderService folderService;

    // ThymLeaf 로그인 페이지 이동
    @GetMapping("/user/login-page")
    public String loginPage() {
        return "login";
    }

    // SignUp 페이지 이동
    @GetMapping("/user/signup")
    public String signupPage() {
        return "signup";
    }

    // User SignUp
    @PostMapping("/user/signup")
    public String signup(@Valid SignupRequestDto requestDto, BindingResult bindingResult) {
        // Validation 예외처리 -> SignupRequestDto Validation -> BindingResult 도출
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if(fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return "redirect:/api/user/signup";
        }

        userService.signup(requestDto);

        return "redirect:/api/user/login-page";
    }

    // 회원 관련 정보 받기
    @GetMapping("/user-info")
    @ResponseBody   // Data 를 RequestBody로 받음
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {  // User Info 가져오기
        String username = userDetails.getUser().getUsername();  // Username
        UserRoleEnum role = userDetails.getUser().getRole();    // Role
        boolean isAdmin = (role == UserRoleEnum.ADMIN); // Check the Role

        return new UserInfoDto(username, isAdmin);
    }

    @GetMapping("/user-folder")
    public String getUserInfo(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails){   // Form 형식으로 데이터를 받기

        model.addAttribute("folders", folderService.getFolders(userDetails.getUser())); // Model에 담아서 뷰단으로 보내주기

        return "index::#fragment";   // 동적으로 데이터를 보내주기 위해서 사용
    }
}