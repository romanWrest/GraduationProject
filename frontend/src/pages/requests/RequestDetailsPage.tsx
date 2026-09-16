import { useMemo, useState } from 'react';
import {
  App,
  Button,
  Card,
  Col,
  DatePicker,
  Descriptions,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Tag,
  Timeline,
  Tooltip,
  Typography,
  Upload,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { DownloadOutlined, PaperClipOutlined, PlusOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useAddComment,
  useCancelRequest,
  useDeleteAttachment,
  useHistory,
  usePatchRequest,
  useReopenRequest,
  useRequest,
  useUploadAttachment,
} from '@/features/requests/hooks';
import { downloadAttachment } from '@/features/requests/api';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_TYPE_LABELS,
  TARGET_POOL_LABELS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatBytes, formatDateTime, shortId } from '@/shared/lib/format';
import { useCurrentUser } from '@/features/auth/hooks';
import { hasAnyRole, hasRole, isExecutor } from '@/shared/lib/roles';
import { ROLES } from '@/shared/constants/roles';
import { useUsers } from '@/features/users/hooks';
import { handleApiError } from '@/shared/lib/handleApiError';
import type { TargetPool } from '@/features/requests/types';

const { Title, Paragraph, Text } = Typography;

type Action =
  | 'ASSIGN'
  | 'REJECT'
  | 'DONE'
  | 'CANCEL'
  | 'REOPEN';

const POOL_TO_ROLE: Record<TargetPool, string> = {
  EXECUTOR_ELECTRIC: 'EXECUTOR_ELECTRIC',
  EXECUTOR_PLUMBING: 'EXECUTOR_PLUMBING',
  EXECUTOR_CARPENTRY: 'EXECUTOR_CARPENTRY',
  EXECUTOR_GAS: 'EXECUTOR_GAS',
  PROPERTY_MANAGER: 'PROPERTY_MANAGER',
  ADMIN: 'ADMIN',
};

export function RequestDetailsPage() {
  const { id = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { message, modal } = App.useApp();
  const { data, isLoading } = useRequest(id);
  const { data: history } = useHistory(id);
  const { data: user } = useCurrentUser();
  const patchMutation = usePatchRequest();
  const cancelMutation = useCancelRequest();
  const reopenMutation = useReopenRequest();
  const addCommentMutation = useAddComment();
  const uploadAttachment = useUploadAttachment();
  const deleteAttachment = useDeleteAttachment();

  const [actionModal, setActionModal] = useState<Action | null>(null);
  const [comment, setComment] = useState('');

  const req = data?.request;

  const isAuthor = !!user && !!req && user.id === req.authorId;
  const isAssignee = !!user && !!req && user.id === req.assigneeId;
  const isAdmin = hasRole(user?.roles, ROLES.ADMIN);

  const canCancel =
    isAuthor && req && (req.status === 'NEW' || req.status === 'IN_REVIEW' || req.status === 'ASSIGNED');
  const canReopen =
    isAuthor && req?.status === 'CLOSED' && req.closedAt &&
    dayjs(req.closedAt).add(7, 'day').isAfter(dayjs());
  const canConfirm = isAuthor && req?.status === 'DONE';
  const canAssign = isAdmin && req && (req.status === 'NEW' || req.status === 'IN_REVIEW' || req.status === 'ASSIGNED');
  const canReview = isAdmin && req?.status === 'NEW';
  const canReject = isAdmin && req && ['NEW', 'IN_REVIEW', 'ASSIGNED'].includes(req.status);
  const canStart =
    (isAssignee || isAdmin) && req?.status === 'ASSIGNED';
  const canDone =
    (isAssignee || isAdmin) && req?.status === 'IN_PROGRESS';
  const canExecutorReject =
    (isAssignee || (isExecutor(user?.roles) && hasAnyRole(user?.roles, [
      ROLES.EXECUTOR_ELECTRIC,
      ROLES.EXECUTOR_PLUMBING,
      ROLES.EXECUTOR_CARPENTRY,
      ROLES.EXECUTOR_GAS,
    ]))) && req && ['ASSIGNED', 'IN_PROGRESS'].includes(req.status);

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!req) {
    return <Empty description="Заявка не найдена" />;
  }

  const onConfirmAction = async () => {
    try {
      await patchMutation.mutateAsync({ id: req.id, payload: { action: 'CONFIRM' } });
      message.success('Подтверждено выполнение');
    } catch (e) {
      handleApiError(e);
    }
  };
  const onStart = async () => {
    try {
      await patchMutation.mutateAsync({ id: req.id, payload: { action: 'START' } });
      message.success('Заявка взята в работу');
    } catch (e) {
      handleApiError(e);
    }
  };
  const onReview = async () => {
    try {
      await patchMutation.mutateAsync({ id: req.id, payload: { action: 'REVIEW' } });
      message.success('Заявка переведена на рассмотрение');
    } catch (e) {
      handleApiError(e);
    }
  };

  const onAddComment = async () => {
    if (!comment.trim()) return;
    try {
      await addCommentMutation.mutateAsync({ id: req.id, body: comment.trim() });
      setComment('');
    } catch (e) {
      handleApiError(e);
    }
  };

  const onUpload = (file: File) => {
    uploadAttachment.mutate(
      { id: req.id, file },
      {
        onSuccess: () => message.success('Файл загружен'),
        onError: (e) => handleApiError(e),
      },
    );
    return false;
  };

  const onDownload = async (attId: string, name: string) => {
    try {
      const blob = await downloadAttachment(req.id, attId);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = name;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      handleApiError(e);
    }
  };

  const onDeleteAttachment = (attId: string) => {
    modal.confirm({
      title: 'Удалить вложение?',
      okText: 'Удалить',
      okButtonProps: { danger: true },
      cancelText: 'Отмена',
      onOk: () =>
        deleteAttachment.mutateAsync({ id: req.id, attId }).catch((e) => handleApiError(e)),
    });
  };

  return (
    <>
      <PageHeader
        title={
          <Space>
            <span>{req.title}</span>
            <Tag color={STATUS_COLORS[req.status]}>{REQUEST_STATUS_LABELS[req.status]}</Tag>
          </Space>
        }
        subtitle={
          <Space>
            <Text type="secondary">ID: {shortId(req.id)}</Text>
            <Text type="secondary">·</Text>
            <Tag>{REQUEST_TYPE_LABELS[req.type]}</Tag>
          </Space>
        }
        extra={
          <Space wrap>
            {canCancel && (
              <Button danger onClick={() => setActionModal('CANCEL')}>
                Отозвать
              </Button>
            )}
            {canConfirm && (
              <Button type="primary" onClick={onConfirmAction} loading={patchMutation.isPending}>
                Подтвердить выполнение
              </Button>
            )}
            {canReopen && (
              <Button onClick={() => setActionModal('REOPEN')}>Переоткрыть</Button>
            )}
            {canReview && (
              <Button onClick={onReview} loading={patchMutation.isPending}>
                На рассмотрение
              </Button>
            )}
            {canAssign && (
              <Button type="primary" onClick={() => setActionModal('ASSIGN')}>
                {req.assigneeId ? 'Переназначить' : 'Назначить'}
              </Button>
            )}
            {canReject && (
              <Button danger onClick={() => setActionModal('REJECT')}>
                Отклонить
              </Button>
            )}
            {canStart && (
              <Button type="primary" onClick={onStart} loading={patchMutation.isPending}>
                Принять в работу
              </Button>
            )}
            {canDone && (
              <Button type="primary" onClick={() => setActionModal('DONE')}>
                Отметить выполненной
              </Button>
            )}
            {canExecutorReject && !isAdmin && (
              <Button danger onClick={() => setActionModal('REJECT')}>
                Отклонить
              </Button>
            )}
            <Button onClick={() => navigate(-1)}>Назад</Button>
          </Space>
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="Описание">
            <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{req.description}</Paragraph>
            {req.reason && (
              <>
                <Title level={5}>Причина</Title>
                <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{req.reason}</Paragraph>
              </>
            )}
            {req.resolutionComment && (
              <>
                <Title level={5}>Комментарий исполнителя</Title>
                <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{req.resolutionComment}</Paragraph>
              </>
            )}
            {req.rejectionReason && (
              <>
                <Title level={5}>Причина отклонения</Title>
                <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{req.rejectionReason}</Paragraph>
              </>
            )}
            {req.cancellationReason && (
              <>
                <Title level={5}>Причина отмены</Title>
                <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{req.cancellationReason}</Paragraph>
              </>
            )}
            {req.reopenReason && (
              <>
                <Title level={5}>Причина переоткрытия</Title>
                <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{req.reopenReason}</Paragraph>
              </>
            )}
          </Card>

          <Card
            title="Комментарии"
            style={{ marginTop: 16 }}
            extra={<Text type="secondary">{data?.comments.length ?? 0}</Text>}
          >
            <List
              dataSource={data?.comments ?? []}
              locale={{ emptyText: 'Пока нет комментариев' }}
              renderItem={(c) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space>
                        <Tooltip title={c.authorId}>
                          <Text>{shortId(c.authorId)}</Text>
                        </Tooltip>
                        <Text type="secondary">{formatDateTime(c.createdAt)}</Text>
                      </Space>
                    }
                    description={<span style={{ whiteSpace: 'pre-wrap' }}>{c.body}</span>}
                  />
                </List.Item>
              )}
            />
            <Form layout="vertical" style={{ marginTop: 12 }}>
              <Form.Item>
                <Input.TextArea
                  rows={3}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  maxLength={5000}
                  showCount
                  placeholder="Добавить комментарий..."
                />
              </Form.Item>
              <Button
                type="primary"
                onClick={onAddComment}
                disabled={!comment.trim()}
                loading={addCommentMutation.isPending}
              >
                Отправить
              </Button>
            </Form>
          </Card>

          {history && history.length > 0 && (
            <Card title="История статусов" style={{ marginTop: 16 }}>
              <Timeline
                items={history.map((h) => ({
                  color: STATUS_COLORS[h.toStatus] === 'default' ? 'gray' : STATUS_COLORS[h.toStatus],
                  children: (
                    <div>
                      <Text strong>
                        {h.fromStatus ? REQUEST_STATUS_LABELS[h.fromStatus] : 'Создание'} →{' '}
                        {REQUEST_STATUS_LABELS[h.toStatus]}
                      </Text>
                      <div>
                        <Text type="secondary">{formatDateTime(h.changedAt)}</Text>
                        {' · '}
                        <Tooltip title={h.actorId}>
                          <Text type="secondary">{shortId(h.actorId)}</Text>
                        </Tooltip>
                      </div>
                      {h.comment && <div style={{ marginTop: 4 }}>{h.comment}</div>}
                    </div>
                  ),
                }))}
              />
            </Card>
          )}
        </Col>

        <Col xs={24} lg={8}>
          <Card title="Информация">
            <Descriptions column={1} size="small" labelStyle={{ width: 140 }}>
              <Descriptions.Item label="Тип">
                {REQUEST_TYPE_LABELS[req.type]}
              </Descriptions.Item>
              <Descriptions.Item label="Целевой пул">
                {TARGET_POOL_LABELS[req.targetPool]}
              </Descriptions.Item>
              <Descriptions.Item label="Автор">
  Иванов И.И.
</Descriptions.Item>
              <Descriptions.Item label="Исполнитель">
  {req.assigneeId ? 'Денисов О.И.' : '—'}
</Descriptions.Item>
              <Descriptions.Item label="Комната">
                102
              </Descriptions.Item>
              <Descriptions.Item label="Запланировано">
                {formatDateTime(req.scheduledAt)}
              </Descriptions.Item>
              <Descriptions.Item label="Создана">{formatDateTime(req.createdAt)}</Descriptions.Item>
              <Descriptions.Item label="Обновлена">{formatDateTime(req.updatedAt)}</Descriptions.Item>
              <Descriptions.Item label="Закрыта">{formatDateTime(req.closedAt)}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card
            title={
              <Space>
                <PaperClipOutlined />
                <span>Вложения</span>
              </Space>
            }
            style={{ marginTop: 16 }}
            extra={
              <Upload showUploadList={false} beforeUpload={onUpload}>
                <Button size="small" icon={<PlusOutlined />} loading={uploadAttachment.isPending}>
                  Добавить
                </Button>
              </Upload>
            }
          >
            {data?.attachments.length ? (
              <List
                size="small"
                dataSource={data.attachments}
                renderItem={(att) => (
                  <List.Item
                    actions={[
                      <Button
                        key="dl"
                        size="small"
                        type="link"
                        icon={<DownloadOutlined />}
                        onClick={() => onDownload(att.id, att.originalName)}
                      >
                        Скачать
                      </Button>,
                      isAdmin || isAuthor ? (
                        <Button
                          key="del"
                          size="small"
                          type="link"
                          danger
                          onClick={() => onDeleteAttachment(att.id)}
                        >
                          Удалить
                        </Button>
                      ) : null,
                    ].filter(Boolean) as React.ReactNode[]}
                  >
                    <List.Item.Meta
                      title={att.originalName}
                      description={
                        <Text type="secondary">
                          {formatBytes(att.sizeBytes)} · {formatDateTime(att.uploadedAt)}
                        </Text>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="Нет вложений" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>

      <ActionModal
        action={actionModal}
        onClose={() => setActionModal(null)}
        targetPool={req.targetPool}
        onAssign={async (assigneeId, scheduledAt) => {
          try {
            await patchMutation.mutateAsync({
              id: req.id,
              payload: { action: 'ASSIGN', assigneeId, scheduledAt },
            });
            message.success('Исполнитель назначен');
            setActionModal(null);
          } catch (e) {
            handleApiError(e);
          }
        }}
        onReject={async (reason) => {
          try {
            await patchMutation.mutateAsync({
              id: req.id,
              payload: { action: 'REJECT', rejectionReason: reason },
            });
            message.success('Заявка отклонена');
            setActionModal(null);
          } catch (e) {
            handleApiError(e);
          }
        }}
        onDone={async (resolutionComment) => {
          try {
            await patchMutation.mutateAsync({
              id: req.id,
              payload: { action: 'DONE', resolutionComment },
            });
            message.success('Заявка отмечена как выполненная');
            setActionModal(null);
          } catch (e) {
            handleApiError(e);
          }
        }}
        onCancel={async (reason) => {
          try {
            await cancelMutation.mutateAsync({ id: req.id, reason });
            message.success('Заявка отозвана');
            setActionModal(null);
          } catch (e) {
            handleApiError(e);
          }
        }}
        onReopen={async (reason) => {
          try {
            await reopenMutation.mutateAsync({ id: req.id, reason });
            message.success('Заявка переоткрыта');
            setActionModal(null);
          } catch (e) {
            handleApiError(e);
          }
        }}
      />
    </>
  );
}

interface ActionModalProps {
  action: Action | null;
  targetPool: TargetPool;
  onClose: () => void;
  onAssign: (assigneeId: string, scheduledAt?: string) => void | Promise<void>;
  onReject: (reason: string) => void | Promise<void>;
  onDone: (resolutionComment: string) => void | Promise<void>;
  onCancel: (reason?: string) => void | Promise<void>;
  onReopen: (reason: string) => void | Promise<void>;
}

function ActionModal(props: ActionModalProps) {
  const { action, onClose, targetPool } = props;
  const [text, setText] = useState('');
  const [assigneeId, setAssigneeId] = useState<string | undefined>();
  const [scheduledAt, setScheduledAt] = useState<string | undefined>();

  const usersFilter = useMemo(
    () => ({ role: POOL_TO_ROLE[targetPool] as never, active: true, size: 100 }),
    [targetPool],
  );
  const { data: users, isLoading: usersLoading } = useUsers(usersFilter);

  const reset = () => {
    setText('');
    setAssigneeId(undefined);
    setScheduledAt(undefined);
  };

  const close = () => {
    reset();
    onClose();
  };

  if (!action) return null;

  if (action === 'ASSIGN') {
    return (
      <Modal
        open
        title="Назначить исполнителя"
        onOk={() => {
          if (!assigneeId) return;
          props.onAssign(assigneeId, scheduledAt);
        }}
        okText="Назначить"
        cancelText="Отмена"
        onCancel={close}
        okButtonProps={{ disabled: !assigneeId }}
      >
        <Form layout="vertical">
          <Form.Item label="Исполнитель" required>
            <Select
              showSearch
              optionFilterProp="label"
              loading={usersLoading}
              value={assigneeId}
              onChange={setAssigneeId}
              placeholder="Выберите исполнителя"
              options={(users?.content ?? []).map((u) => ({
                label: `${u.fullName} (${u.email})`,
                value: u.id,
              }))}
            />
          </Form.Item>
          <Form.Item label="Дата визита">
            <DatePicker
              showTime={{ format: 'HH:mm' }}
              format="DD.MM.YYYY HH:mm"
              style={{ width: '100%' }}
              onChange={(d) => setScheduledAt(d ? d.toISOString() : undefined)}
            />
          </Form.Item>
        </Form>
      </Modal>
    );
  }

  if (action === 'REJECT') {
    return (
      <Modal
        open
        title="Отклонить заявку"
        onOk={() => props.onReject(text)}
        okText="Отклонить"
        okButtonProps={{ danger: true, disabled: !text.trim() }}
        cancelText="Отмена"
        onCancel={close}
      >
        <Input.TextArea
          rows={4}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Причина отклонения"
          maxLength={1000}
          showCount
        />
      </Modal>
    );
  }

  if (action === 'DONE') {
    return (
      <Modal
        open
        title="Отметить выполненной"
        onOk={() => props.onDone(text)}
        okText="Отправить"
        okButtonProps={{ disabled: !text.trim() }}
        cancelText="Отмена"
        onCancel={close}
      >
        <Input.TextArea
          rows={4}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Что сделано"
          maxLength={2000}
          showCount
        />
      </Modal>
    );
  }

  if (action === 'CANCEL') {
    return (
      <Modal
        open
        title="Отозвать заявку"
        onOk={() => props.onCancel(text || undefined)}
        okText="Отозвать"
        okButtonProps={{ danger: true }}
        cancelText="Отмена"
        onCancel={close}
      >
        <Input.TextArea
          rows={3}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Причина (необязательно)"
          maxLength={1000}
          showCount
        />
      </Modal>
    );
  }

  if (action === 'REOPEN') {
    return (
      <Modal
        open
        title="Переоткрыть заявку"
        onOk={() => props.onReopen(text)}
        okText="Переоткрыть"
        okButtonProps={{ disabled: !text.trim() }}
        cancelText="Отмена"
        onCancel={close}
      >
        <Input.TextArea
          rows={3}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Причина переоткрытия"
          maxLength={1000}
          showCount
        />
      </Modal>
    );
  }

  return null;
}
