import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = 'YxinMiracle';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`${currentYear} ${defaultMessage}`}
      links={[
        {
          key: 'codeNav',
          title: '个人网站',
          href: 'http://www.yxinmiracle.com',
          blankTarget: true,
        },
        {
          key: 'Ant Design',
          title: '个人博客',
          href: 'https://blog.csdn.net/caiyongxin_001',
          blankTarget: true,
        },
        {
          key: 'github',
          title: (
            <>
              <GithubOutlined /> YxinMiracleGithub
            </>
          ),
          href: 'https://github.com/YxinMiracle',
          blankTarget: true,
        },
      ]}
    />
  );
};
export default Footer;
