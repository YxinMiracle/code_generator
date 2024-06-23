import { PageContainer, ProFormSelect, ProFormText, QueryFilter } from '@ant-design/pro-components';
import { listGeneratorVoByPageUsingPost } from '@/services/backend/generatorController';
import React, { useEffect, useState } from 'react';
import { Avatar, Card, Flex, Image, Input, List, message, Tabs, Tag, Typography } from 'antd';
import moment from 'moment';
import { UserOutlined } from '@ant-design/icons';
import {Link} from "umi";

// 默认的分页参数
const DEFAULT_PAGE_PARAMS: PageRequest = {
  current: 1,
  pageSize: 4,
  sortField: 'createTime',
  sortOrder: 'descend',
};

const IndexPage: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [dataList, setDataList] = useState<API.GeneratorVO[]>([]);
  const [total, setTotal] = useState<number>(0);
  // 搜索条件
  const [searchParams, setSearchParams] = useState<API.GeneratorQueryRequest>({
    ...DEFAULT_PAGE_PARAMS,
  });

  const doSearch = async () => {
    setLoading(true);
    try {
      const res = await listGeneratorVoByPageUsingPost(searchParams);
      setDataList(res?.data?.records ?? []);
      setTotal(Number(res.data?.total) ?? 0);
    } catch (error: any) {
      message.error(error.message);
    }
    setLoading(false);
  };

  /*
    tag处理
   */
  const tagListView = (tags?: string[]) => {
    if (!tags) {
      return (<></>);
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

  useEffect(() => {
    doSearch();
  }, [searchParams]);
  // @ts-ignore
  return (
    <PageContainer title={<></>}>
      <Flex>
        <Input.Search
          placeholder="请搜索生成器"
          style={{ width: '40vw', minWidth: 320, margin: 'auto' }}
          allowClear
          enterButton="搜索"
          size="large"
          value={searchParams.searchText}
          onChange={(e) => {
            searchParams.searchText = e.target.value;
          }}
          onSearch={(value: string) => {
            setSearchParams({
              ...DEFAULT_PAGE_PARAMS,
              searchText: value,
            });
          }}
        />
      </Flex>
      <div style={{ marginBottom: '16px' }}></div>
      <Tabs
        defaultActiveKey="newest"
        items={[
          {
            key: 'newest',
            label: '最新',
          },
          {
            key: 'recommend',
            label: '推荐',
          },
        ]}
        onChange={() => {}}
      />

      <QueryFilter
        span={8}
        labelWidth="auto"
        split
        labelAlign="left"
        style={{ padding: '16px 0' }}
        onFinish={async (values: API.GeneratorQueryRequest) => {
          setSearchParams({
            ...DEFAULT_PAGE_PARAMS,
            searchText: searchParams.searchText,
            ...values,
          });
        }}
      >
        <ProFormSelect label="标签" name="tags" mode="tags"></ProFormSelect>
        <ProFormText label="名称" name="name" />
        <ProFormText label="描述" name="description" />
      </QueryFilter>

      <List<API.GeneratorVO>
        rowKey="id"
        loading={loading}
        grid={{
          gutter: 16,
          xs: 1,
          sm: 2,
          md: 3,
          lg: 3,
          xl: 4,
          xxl: 4,
        }}
        dataSource={dataList}
        pagination={{
          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total,
          onChange: (current, pageSize) => {
            setSearchParams({
              ...searchParams,
              current,
              pageSize,
            });
          },
        }}
        renderItem={(data) => (
          <List.Item>
            <Link to={`/generator/detail/${data.id}`}>
              <Card hoverable cover={<Image alt={data.name} src={data.picture} />}>
                <Card.Meta
                  title={<a>{data.name}</a>}
                  description={
                    <Typography.Paragraph
                      ellipsis={{
                        rows: 2,
                      }}
                      style={{ height: 44 }}
                    >
                      {data.description}
                    </Typography.Paragraph>
                  }
                />
                {tagListView(data.tags)}
                <Flex justify="space-between" align="center">
                  <Typography.Paragraph type="secondary" style={{ fontSize: 12 }}>{moment(data.createTime).fromNow()}</Typography.Paragraph>
                  <div>
                    <Avatar src={data?.user?.userAvatar ?? <UserOutlined/>}/>
                  </div>
                </Flex>

              </Card>

            </Link>
          </List.Item>
        )}
      />
    </PageContainer>
  );
};

export default IndexPage;
