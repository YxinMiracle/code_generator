package com.yxinmiracle.model;

/*
* @author  yxinmiracle
* @date  2024-06-06 15:32
* @Gitee: https://gitee.com/yxinmiracle
* 数据模型
*/
import lombok.Data;


@Data
public class DataModel {


    /*
    * 是否生成循环
    */
    private boolean loop  = false;


    /*
    * 作者注释
    */
    private String author  = "yxinmriacle";


    /*
    * 输出信息
    */
    private String outputText  = "sum = ";


}
