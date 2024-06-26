import {
  ProCard,
  ProFormInstance,
  ProFormItem,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  StepsForm,
} from '@ant-design/pro-components';
import {message, UploadFile} from 'antd';
import {useEffect, useRef, useState} from 'react';
import PictureUploader from '@/components/PictureUploader';
import FileUploader from '@/components/FileUploader';
import {
  addGeneratorUsingPost,
  editGeneratorUsingPost,
  getGeneratorVoByIdUsingGet,
} from '@/services/backend/generatorController';
import {history} from '@umijs/max';
import {useSearchParams} from '@@/exports';
import {COS_HOST} from '@/constants';
import ModelConfigForm from "@/pages/Generator/Add/components/ModelConfigForm";

/**
 * 生成器创建页面
 * @constructor
 */
const GeneratorAddPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const id = searchParams.get('id');
  const [oldData, setOldData] = useState<API.GeneratorEditRequest>();

  const formRef = useRef<ProFormInstance>();

  const loadData = async () => {
    if (!id) {
      return;
    }
    try {
      // @ts-ignore
      const res = await getGeneratorVoByIdUsingGet({ id });
      if (res.data) {
        const { distPath } = res.data ?? {};
        if (distPath) {
          // @ts-ignore
          res.data.distPath = [
            {
              uid: id,
              name: '文件' + id,
              status: 'done',
              url: COS_HOST + distPath,
              response: distPath,
            } as UploadFile,
          ];
        }
        setOldData(res.data);
      }
    } catch (error: any) {
      message.error('加载数据失败' + error.message);
    }
  };

  useEffect(() => {
    if (!id) {
      return;
    }
    loadData();
  }, [id]);

  // 这是添加generator数据接口
  const doAdd = async (values: API.GeneratorAddRequest) => {
    // 调用接口
    try {
      const res = await addGeneratorUsingPost(values);
      if (res.data) {
        message.success('创建成功');
        history.push(`/generator/detail/${res.data}`);
      }
    } catch (error: any) {
      message.error('创建失败', error.message);
    }
  };

  const doUpdate = async (values: API.GeneratorEditRequest) => {
    // 调用接口
    try {
      const res = await editGeneratorUsingPost(values);
      if (res.data) {
        message.success('更新成功');
        history.push(`/generator/detail/${id}`);
      }
    } catch (error: any) {
      message.error('更新失败', error.message);
    }
  };

  const dosubmit = async (values: API.GeneratorAddRequest) => {
    if (!values.fileConfig) {
      values.fileConfig = {};
    }
    if (!values.modelConfig) {
      values.modelConfig = {};
    }

    if (values.distPath && values.distPath.length > 0) {
      // @ts-ignore
      values.distPath = values.distPath[0].response;
    }

    if (id) {
      await doUpdate({
        // @ts-ignore
        id: id,
        ...values,
      });
    } else {
      await doAdd(values);
    }
  };

  return (
    <ProCard>
      {/*是创建而不是更新， 这里主要是用来控制，要是是更新逻辑的话，可能导致在页面加载的过程中，oldData为空值，导致页面不会自动的更新*/}
      {(!id || oldData) && (
        <StepsForm<API.GeneratorAddRequest | API.GeneratorEditRequest>
          formRef={formRef}
          onFinish={dosubmit}
          formProps={{
            initialValues: oldData,
          }}
        >
          <StepsForm.StepForm
            name="base"
            title="基本信息"
            onFinish={async () => {
              console.log(formRef.current?.getFieldsValue());
              return true;
            }}
          >
            <ProFormText name="name" label="名称" placeholder="请输入名称" />
            <ProFormTextArea name="description" label="描述" placeholder="请输入描述" />
            <ProFormText name="basePackage" label="基础包" placeholder="请输入基础包" />
            <ProFormText name="version" label="版本号" placeholder="请输入版本号" />
            <ProFormText name="author" label="作者" placeholder="请输入作者" />
            <ProFormSelect label="标签" mode="tags" name="tags" placeholder="请输入标签列表" />
            <ProFormItem label="图片" name="picture">
              <PictureUploader biz="generator_picture" />
            </ProFormItem>
          </StepsForm.StepForm>
          <StepsForm.StepForm
            name="fileConfig"
            title="文件配置"
            // todo 待补充
          ></StepsForm.StepForm>
          <StepsForm.StepForm
            name="modelConfig"
            title="模型配置"
            onFinish={async (values)=>{
              console.log(values)
              return true
            }}
            // todo 待补充
          >
            <ModelConfigForm formRef={formRef} oldData={oldData}></ModelConfigForm>
          </StepsForm.StepForm>
          <StepsForm.StepForm name="dist" title="生成器文件">
            <ProFormItem label="产物包" name="distPath">
              <FileUploader biz="generator_dist" description="请上传生成器文件压缩包" />
            </ProFormItem>
          </StepsForm.StepForm>
        </StepsForm>
      )}
    </ProCard>
  );
};

export default GeneratorAddPage;
