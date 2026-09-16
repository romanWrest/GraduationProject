import { useEffect, useState } from 'react';
import { App, Button, Card, Space, Switch, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import { useSettings, useUpdateSettings } from '@/features/notifications/hooks';
import { NOTIFICATION_TYPE_LABELS } from '@/shared/constants/notifications';
import type { NotificationType } from '@/features/notifications/types';
import { handleApiError } from '@/shared/lib/handleApiError';

const { Paragraph } = Typography;

interface Row {
  type: NotificationType;
  email: boolean;
  inApp: boolean;
}

export function NotificationSettingsPage() {
  const { message } = App.useApp();
  const { data, isLoading } = useSettings();
  const update = useUpdateSettings();

  const [emailEnabled, setEmailEnabled] = useState(true);
  const [inAppEnabled, setInAppEnabled] = useState(true);
  const [byType, setByType] = useState<Record<string, Record<string, boolean>>>({});

  useEffect(() => {
    if (data) {
      setEmailEnabled(data.emailEnabled);
      setInAppEnabled(data.inAppEnabled);
      setByType(data.byType ?? {});
    }
  }, [data]);

  const rows: Row[] = (Object.keys(NOTIFICATION_TYPE_LABELS) as NotificationType[]).map((t) => ({
    type: t,
    email: byType[t]?.email ?? true,
    inApp: byType[t]?.inApp ?? true,
  }));

  const onSave = async () => {
    try {
      await update.mutateAsync({ emailEnabled, inAppEnabled, byType });
      message.success('Настройки сохранены');
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<Row> = [
    {
      title: 'Тип',
      dataIndex: 'type',
      render: (t: NotificationType) => NOTIFICATION_TYPE_LABELS[t],
    },
    {
      title: 'Email',
      dataIndex: 'email',
      width: 120,
      render: (v: boolean, r) => (
        <Switch
          checked={v}
          disabled={!emailEnabled}
          onChange={(checked) =>
            setByType({
              ...byType,
              [r.type]: { ...byType[r.type], email: checked },
            })
          }
        />
      ),
    },
    {
      title: 'In-app',
      dataIndex: 'inApp',
      width: 120,
      render: (v: boolean, r) => (
        <Switch
          checked={v}
          disabled={!inAppEnabled}
          onChange={(checked) =>
            setByType({
              ...byType,
              [r.type]: { ...byType[r.type], inApp: checked },
            })
          }
        />
      ),
    },
  ];

  return (
    <>
      <PageHeader title="Настройки уведомлений" />
      <Card title="Каналы">
        <Space size={32}>
          <div>
            <Paragraph style={{ marginBottom: 4 }}>Email</Paragraph>
            <Switch checked={emailEnabled} onChange={setEmailEnabled} />
          </div>
          <div>
            <Paragraph style={{ marginBottom: 4 }}>In-app</Paragraph>
            <Switch checked={inAppEnabled} onChange={setInAppEnabled} />
          </div>
        </Space>
      </Card>
      <Card title="Типы уведомлений" style={{ marginTop: 16 }}>
        <Table<Row>
          rowKey="type"
          columns={columns}
          dataSource={rows}
          loading={isLoading}
          pagination={false}
        />
      </Card>
      <div style={{ marginTop: 16 }}>
        <Button type="primary" onClick={onSave} loading={update.isPending}>
          Сохранить
        </Button>
      </div>
    </>
  );
}
