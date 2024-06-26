import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  Collapse,
  Form,
  Image,
  Input,
  message,
  Row,
  Space,
  Tag,
  Typography,
} from 'antd';
import React, { useEffect, useState } from 'react';
import {
  getGeneratorVoByIdUsingGet,
  useGeneratorUsingPost,
} from '@/services/backend/generatorController';
import { useParams } from '@@/exports';
import { DownlandOutline } from 'antd-mobile-icons';
import { COS_HOST } from '@/constants';
import { saveAs } from 'file-saver';
import { useModel } from '@umijs/max';
import { Link } from 'umi';

/**
 * 生成器详情页
 * @constructor
 */
const GeneratorUsePage: React.FC = () => {
  const { id } = useParams();
  const [loading, setLoading] = useState<boolean>(false);
  const [downloading, setDownloading] = useState<boolean>(false);
  const [data, setData] = useState<API.GeneratorVO>({});
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [form] = Form.useForm();

  const models = data?.modelConfig?.models ?? [];

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
      type="primary"
      icon={<DownlandOutline />}
      loading={downloading}
      onClick={async () => {
        setDownloading(true);
        let values = form.getFieldsValue();
        // eslint-disable-next-line react-hooks/rules-of-hooks
        const blob = await useGeneratorUsingPost(
          // @ts-ignore
          {
            id: data.id,
            dataModel: values,
          },
          {
            responseType: 'blob',
          },
        );
        const fullPath = COS_HOST + data.distPath;
        setDownloading(false);
        saveAs(blob, fullPath.substring(fullPath.lastIndexOf('/') + 1));
      }}
    >
      生成代码
    </Button>
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

            <div style={{ marginBottom: 24 }}></div>
            <Form form={form}>
              {models.map((model, index) => {
                if (model.groupKey) {
                  if (!model.models) {
                    return <></>;
                  }
                  return (
                    <Collapse
                      style={{ marginBottom: 24 }}
                      key={index}
                      items={[
                        {
                          key: index,
                          label: model.groupName + '(分组)',
                          children: model.models?.map((subModel, index) => {
                            return (
                              <Form.Item
                                key={index}
                                label={subModel.fieldName}
                                // @ts-ignore
                                name={[model.groupKey, subModel.fieldName]}
                              >
                                <Input placeholder={subModel.description}></Input>
                              </Form.Item>
                            );
                          }),
                        },
                      ]}
                      bordered={false}
                      defaultActiveKey={[index]}
                    />
                  );
                }
                return (
                  <Form.Item key={index} label={model.fieldName} name={model.fieldName}>
                    <Input placeholder={model.description}></Input>
                  </Form.Item>
                );
              })}
            </Form>
            <Space>
              {downloadButton}
              <Link to={`/generator/detail/${id}`}>
                <Button>查看详情</Button>
              </Link>
            </Space>
          </Col>
          <Col flex="320px">
            <Image src={data.picture}></Image>
          </Col>
        </Row>
      </Card>

      <div style={{ marginBottom: 24 }}></div>
    </PageContainer>
  );
};

export default GeneratorUsePage;
