package com.changgou.file.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.pojo.FastDFSFile;
import com.changgou.file.util.FastDFSClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/file")
public class FileController {
    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile multipartFile){
        try {

            if (multipartFile==null){
                throw new RuntimeException("文件不存在");
            }
            //文件完整名称
            String filename = multipartFile.getOriginalFilename();
            if (StringUtils.isEmpty(filename)){
                throw new RuntimeException("文件不存在");
            }
            //文件扩展名
            String name = filename.substring(filename.lastIndexOf(".") + 1);
            //文件内容
            byte[] bytes = multipartFile.getBytes();
            //创建FastDFSFile实体类
            FastDFSFile fastDFSFile = new FastDFSFile(filename,bytes,name);

            //文件上传
            String[] upload = FastDFSClient.upload(fastDFSFile);
            //封装结果
            String url = FastDFSClient.getTrackerUrl()+upload[0]+"/"+upload[1];

            return new Result(true, StatusCode.OK,"文件上传成功",url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Result(false,StatusCode.ERROR,"文件上传失败");
    }
}
