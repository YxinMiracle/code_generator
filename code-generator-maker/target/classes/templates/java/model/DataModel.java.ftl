package ${basePackage}.model;

/*
* @author  ${author}
* @date  2024-06-06 15:32
* @Gitee: https://gitee.com/yxinmiracle
* 数据模型
*/
import lombok.Data;

<#macro generateModel indent modelInfo>
<#if modelInfo.description??>
${indent}/*
${indent}* ${modelInfo.description}
${indent}*/
</#if>
${indent}public ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
</#macro>

@Data
public class DataModel {

<#list modelConfig.models as modelInfo>

    <#if modelInfo.groupKey??>
    /*
     * ${modelInfo.description}
     */
    public ${modelInfo.type} ${modelInfo.groupKey} = new ${modelInfo.type}();

    // 构建${modelInfo.description}内部类
    @Data
    public static class ${modelInfo.type} {
    <#list modelInfo.models as subModelInfo>
        <@generateModel indent="        " modelInfo=subModelInfo />
    </#list>
    }

    <#else>
    <@generateModel indent="    " modelInfo=modelInfo />

    </#if>
</#list>

}