import { PageContainer } from '@ant-design/pro-components';
import { Button, Card, Col, Image, message, Row, Space, Tabs, Tag, Typography } from 'antd';
import React, { useEffect, useState } from 'react';
import {
  downloadGeneratorByIdUsingGet,
  getGeneratorVoByIdUsingGet,
} from '@/services/backend/generatorController';
import { useParams } from '@@/exports';
import moment from 'moment';
import { DownlandOutline } from 'antd-mobile-icons';
import FileConfig from '@/pages/Generator/Detail/components/FileConfig';
import ModelConfig from '@/pages/Generator/Detail/components/ModelConfig';
import AuthorInfo from '@/pages/Generator/Detail/components/AuthorInfo';
import { EditOutlined } from '@ant-design/icons';
import { testDownloadFileUsingGet } from '@/services/backend/fileController';
import { COS_HOST } from '@/constants';
import { saveAs } from 'file-saver';
import { useModel } from '@umijs/max';
import { Link } from 'umi';

/**
 * 生成器详情页
 * @constructor
 */
const GeneratorDetailPage: React.FC = () => {
  const { id } = useParams();
  const [loading, setLoading] = useState<boolean>(false);
  const [data, setData] = useState<API.GeneratorVO>({});
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const my = currentUser?.id === data?.userId;

  const loadData = async () => {
    if (!id) {
      return;
    }
    setLoading(true);
    try {
      // @ts-ignore
      const res = await getGeneratorVoByIdUsingGet({ id });
      setData(res.data ?? {});
    } catch (error: any) {
      message.error('加载数据失败' + error.message);
    }
    setLoading(false);
  };
  // todo 动态路径和?是两个不一样的东西
  useEffect(() => {
    if (!id) {
      return;
    }
    loadData();
  }, [id]);

  const downloadButton = data.distPath && currentUser && (
    <Button
      icon={<DownlandOutline />}
      onClick={async () => {
        const blob = await downloadGeneratorByIdUsingGet(
          { id },
          {
            responseType: 'blob',
          },
        );
        const fullPath = COS_HOST + data.distPath;
        saveAs(blob, fullPath.substring(fullPath.lastIndexOf('/') + 1));
      }}
    >
      下载
    </Button>
  );

  const editButton = my && (
    <Link to={`/generator/update?id=${data.id}`}>
      <Button icon={<EditOutlined />}>编辑</Button>
    </Link>
  );

  /*
  tag处理
 */
  const tagListView = (tags?: string[]) => {
    if (!tags) {
      return <></>;
    } else {
      return (
        <div style={{ marginBottom: 8 }}>
          {tags.map((tag: string) => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </div>
      );
    }
  };

  return (
    <PageContainer title={<></>} loading={loading}>
      <Card>
        <Row justify="space-between" gutter={[32, 32]}>
          <Col flex="auto">
            <Space size="large" align="center">
              <Typography.Title level={4}>{data.name}</Typography.Title>
              {tagListView(data.tags)}
            </Space>
            <Typography.Paragraph>{data.description}</Typography.Paragraph>
            <Typography.Paragraph type="secondary">
              创建时间: {moment(data.createTime).format('YYYY-MM-DD HH:mm:ss')}
            </Typography.Paragraph>
            <Typography.Paragraph type="secondary">基础包: {data.basePackage}</Typography.Paragraph>
            <Typography.Paragraph type="secondary">版本: {data.version}</Typography.Paragraph>
            <Typography.Paragraph type="secondary">作者: {data.author}</Typography.Paragraph>
            <div style={{ marginBottom: 24 }}></div>
            <Space>
              <Button type="primary">立即使用</Button>
              {downloadButton}
              {editButton}
            </Space>
          </Col>
          <Col flex="320px">
            <Image src={data.picture}></Image>
          </Col>
        </Row>
      </Card>

      <div style={{ marginBottom: 24 }}></div>

      <Card>
        <Tabs
          size="large"
          defaultActiveKey={'fileConfig'}
          onChange={() => {}}
          items={[
            {
              key: 'fileConfig',
              label: '文件配置',
              children: <FileConfig data={data} />,
            },
            {
              key: 'modelConfig',
              label: '模型配置',
              children: <ModelConfig data={data} />,
            },
            {
              key: 'userInfo',
              label: '作者信息',
              children: <AuthorInfo data={data} />,
            },
          ]}
        ></Tabs>
      </Card>
    </PageContainer>
  );
};

export default GeneratorDetailPage;
