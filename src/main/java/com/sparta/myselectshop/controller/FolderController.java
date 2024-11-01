package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.FolderRequestDto;
import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping("/folders")
    public void addFolders(
            @RequestBody FolderRequestDto folderRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            // Authentication 에서 User Info 가져오는 어노테이션
    ){
        List<String> folderNames = folderRequestDto.getFolderNames();
        folderService.addFolders(folderNames, userDetails.getUser());
        // FolderName 과 User Info -> Service
    }

    @GetMapping("/folders")
    public List<FolderResponseDto> getFolders(@AuthenticationPrincipal UserDetailsImpl userDetails){
        // @AuthenticationPrincipal => Authentication 의 유저 정보를 가져오는 어노테이션
        return folderService.getFolders(userDetails.getUser());
        // 하나의 Folder 만 가져오면 되기 때문에 유저 정보만 Service 단으로 보내주면 됨
    }
}
