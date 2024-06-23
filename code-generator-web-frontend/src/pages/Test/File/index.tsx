import {Button, Card, Divider, Flex, message, UploadProps} from 'antd';
import Dragger from 'antd/es/upload/Dragger';
import {InboxOutlined} from '@ant-design/icons';
import {testDownloadFileUsingGet, testUploadFileUsingPost,} from '@/services/backend/fileController';
import {useState} from 'react';
import {COS_HOST} from '@/constants';
import {saveAs} from 'file-saver';

/**
 * 测试文件上传界面
 * @constructor
 */
const TestFilePage: React.FC = () => {
  const [value, setValue] = useState<string>('');

  const props: UploadProps = {
    name: 'file',
    multiple: false,
    maxCount: 1, // 只上传一个文件
    // todo
    customRequest: async (fileObj: any) => {
      try {
        const res = await testUploadFileUsingPost({}, fileObj.file);
        fileObj.onSuccess(res.data);
        setValue(res.data as string);
      } catch (error: any) {
        message.error('上传失败：' + error.message);
        fileObj.onError(error);
      }
    },
    onRemove() {
      setValue("");
    },
  };
  return (
    <Flex gap={16}>
      <Card title="文件上传">
        <Dragger {...props}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for a single or bulk upload. Strictly prohibited from uploading company data or
            other banned files.
          </p>
        </Dragger>
      </Card>
      <Card title="文件下载">
        <div>文件地址: {COS_HOST + value}</div>
        <Divider></Divider>
        <img src={COS_HOST + value} height={200}></img>
        <Divider></Divider>
        <Button
          // todo
          onClick={async () => {
            const blob = await testDownloadFileUsingGet(
              { filePath: value },
              {
                responseType: 'blob',
              },
            );
            const fullPath = COS_HOST + value;
            saveAs(blob, fullPath.substring(fullPath.lastIndexOf('/') + 1));
          }}
        >
          点击下载文件
        </Button>
      </Card>
    </Flex>
  );
};
export default TestFilePage;
