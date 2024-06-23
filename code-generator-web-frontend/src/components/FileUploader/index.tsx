import { message, UploadFile, UploadProps } from 'antd';
import Dragger from 'antd/es/upload/Dragger';
import { InboxOutlined } from '@ant-design/icons';
import { uploadFileUsingPost } from '@/services/backend/fileController';
import { useState } from 'react';

interface Props {
  biz: string;
  onChange?: (fileList: UploadFile[]) => void; // 子组件上传完之后，可以和外界说，这已经改好了
  value?: UploadFile[];
  description?: string;
}

/**
 * 文件上传界面
 * @constructor
 */
const FileUploader: React.FC<Props> = (props) => {
  const { biz, value, description, onChange } = props;
  const [loading, setLoading] = useState<boolean>(false);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    listType: 'text',
    disabled: loading,
    maxCount: 1, // 只上传一个文件
    fileList: value,
    onChange({ fileList }) {
      onChange?.(fileList);
    },
    // todo
    customRequest: async (fileObj: any) => {
      setLoading(true);
      try {
        const res = await uploadFileUsingPost(
          {
            biz,
          },
          {},
          fileObj.file,
        );
        fileObj.onSuccess(res.data);
      } catch (error: any) {
        message.error('上传失败：' + error.message);
        fileObj.onError(error);
      }
      setLoading(false);
    },
  };
  return (
    <Dragger {...uploadProps}>
      <p className="ant-upload-drag-icon">
        <InboxOutlined />
      </p>
      <p className="ant-upload-text">点击或者拖拽文件上传</p>
      <p className="ant-upload-hint">{description}</p>
    </Dragger>
  );
};
export default FileUploader;
