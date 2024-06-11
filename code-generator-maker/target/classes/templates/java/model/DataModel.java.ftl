package ${basePackage}.model;

/*
* @author  ${author}
* @date  2024-06-06 15:32
* @Gitee: https://gitee.com/yxinmiracle
* 数据模型
*/
import lombok.Data;


@Data
public class DataModel {

<#list modelConfig.models as modelInfo>

    <#if modelInfo.description??>
    /*
    * ${modelInfo.description}
    */
    </#if>
    private ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;

</#list>

}
