import { message, Upload, UploadProps } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { uploadFileUsingPost } from '@/services/backend/fileController';
import { useState } from 'react';
import { COS_HOST } from '@/constants';

interface Props {
  biz: string;
  onChange?: (fileList: string) => void; // 子组件上传完之后，可以和外界说，这已经改好了
  value?: string;
}

/**
 * 文件上传界面
 * @constructor
 */
const FileUploader: React.FC<Props> = (props) => {
  const { biz, value, onChange } = props;
  const [loading, setLoading] = useState<boolean>(false);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    listType: 'picture-card',
    showUploadList: false,
    disabled: loading,
    maxCount: 1, // 只上传一个文件
    // fileList: value,
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
        const fullPath = COS_HOST + res.data
        onChange?.(fullPath ?? "")
        fileObj.onSuccess(res.data);

      } catch (error: any) {
        message.error('上传失败：' + error.message);
        fileObj.onError(error);
      }
      setLoading(false);
    },
  };

  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type="button">
      <PlusOutlined />
      <div style={{ marginTop: 8 }}>上传</div>
    </button>
  );

  return (
    <Upload {...uploadProps}>
      {value ? <img src={value} alt="picture" style={{ width: '100%' }}></img> : uploadButton}
    </Upload>
  );
};
export default FileUploader;
