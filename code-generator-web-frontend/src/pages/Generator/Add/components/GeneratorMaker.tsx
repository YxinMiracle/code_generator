import { ProForm } from '@ant-design/pro-form';
import { ProFormInstance, ProFormItem } from '@ant-design/pro-components';
import FileUploader from '@/components/FileUploader';
import { useRef } from 'react';
import { Collapse, message } from 'antd';
import { val } from '@umijs/utils/compiled/cheerio/lib/api/attributes';
import {
  makeGeneratorUsingPost,
  useGeneratorUsingPost,
} from '@/services/backend/generatorController';
import { makeGroupedTag } from 'styled-components/dist/sheet/GroupedTag';
import { saveAs } from 'file-saver';

interface Props {
  meta: any;
}

export default (props: Props) => {
  const { meta } = props;

  const formRef = useRef<ProFormInstance>();

  const dosubmit = async (values: API.GeneratorMakeRequests) => {
    if (!meta.name) {
      message.error('请填写名称');
      return;
    }
    const zipFilePath = values.zipFilePath;
    if (!zipFilePath || zipFilePath.length < 1) {
      message.error('请上传模板文件压缩包');
      return;
    }

    //@ts-ignore
    values.zipFilePath = zipFilePath[0].response;

    try {
      const blob = await makeGeneratorUsingPost(
        // @ts-ignore
        {
          meta,
          zipFilePath: values.zipFilePath,
        },
        {
          responseType: 'blob',
        },
      );

      saveAs(blob, meta.name + '.zip');
    } catch (e: any) {
      message.error("下载压缩包异常" + e.message)
    }
  };

  const formView = (
    <ProForm
      formRef={formRef}
      submitter={{
        searchConfig: {
          submitText: '制作',
        },
        resetButtonProps: {
          hidden: true,
        },
      }}
      onFinish={dosubmit}
    >
      <ProFormItem label="上传模板文件" name="zipFilePath">
        <FileUploader
          biz="generator_make_template"
          description="请上传压缩包，打包时不要添加最外层目录"
        />
      </ProFormItem>
    </ProForm>
  );

  return (
    <Collapse
      items={[
        {
          key: '1',
          label: '生成器注册工具',
          children: formView,
        },
      ]}
    />
  );
};
