import { useState } from 'react';
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
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Tag,
  Timeline,
  Tooltip,
  Typography,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import dayjs from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useEvictResident,
  useMoveResident,
  useResident,
  useResidentHistory,
  useUpdateResident,
} from '@/features/residents/hooks';
import { useRooms } from '@/features/rooms/hooks';
import { RESIDENT_KIND_LABELS } from '@/shared/constants/residents';
import { formatDate, formatDateTime, shortId } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';

const { Text, Paragraph } = Typography;

type Action = 'EVICT' | 'MOVE' | 'EDIT';

export function ResidentDetailsPage() {
  const { id = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { message } = App.useApp();
  const { data: resident, isLoading } = useResident(id);
  const { data: history } = useResidentHistory(id);
  const { data: rooms } = useRooms({ hasFreeBeds: true, size: 200 });
  const evict = useEvictResident();
  const move = useMoveResident();
  const update = useUpdateResident();

  const [action, setAction] = useState<Action | null>(null);

  const [evictAt, setEvictAt] = useState<string>(dayjs().format('YYYY-MM-DD'));
  const [evictReason, setEvictReason] = useState('');

  const [moveRoomId, setMoveRoomId] = useState<string | undefined>();
  const [moveAt, setMoveAt] = useState<string>(dayjs().format('YYYY-MM-DD'));
  const [moveReason, setMoveReason] = useState('');

  const [edit, setEdit] = useState({
    faculty: '',
    studyGroup: '',
    department: '',
    phone: '',
    contactInfo: '',
  });

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }
  if (!resident) {
    return <Empty description="Жилец не найден" />;
  }

  const onEvict = async () => {
    try {
      await evict.mutateAsync({
        id,
        payload: { evictedAt: evictAt, reason: evictReason || undefined },
      });
      message.success('Жилец выселен');
      setAction(null);
    } catch (e) {
      handleApiError(e);
    }
  };
  const onMove = async () => {
    if (!moveRoomId) return;
    try {
      await move.mutateAsync({
        id,
        payload: { newRoomId: moveRoomId, movedAt: moveAt, reason: moveReason || undefined },
      });
      message.success('Жилец переселён');
      setAction(null);
    } catch (e) {
      handleApiError(e);
    }
  };
  const onEdit = async () => {
    try {
      await update.mutateAsync({
        id,
        payload: {
          faculty: edit.faculty || undefined,
          studyGroup: edit.studyGroup || undefined,
          department: edit.department || undefined,
          phone: edit.phone || undefined,
          contactInfo: edit.contactInfo || undefined,
        },
      });
      message.success('Данные обновлены');
      setAction(null);
    } catch (e) {
      handleApiError(e);
    }
  };

  const isActive = !resident.evictedAt;

  return (
    <>
      <PageHeader
        title={
          <Space>
            <span>Жилец {shortId(resident.id)}</span>
            <Tag color={isActive ? 'green' : 'default'}>{isActive ? 'Активен' : 'Выселен'}</Tag>
            <Tag>{RESIDENT_KIND_LABELS[resident.kind]}</Tag>
          </Space>
        }
        extra={
          <Space>
            <Button
              onClick={() => {
                setEdit({
                  faculty: resident.faculty ?? '',
                  studyGroup: resident.studyGroup ?? '',
                  department: resident.department ?? '',
                  phone: resident.phone ?? '',
                  contactInfo: resident.contactInfo ?? '',
                });
                setAction('EDIT');
              }}
            >
              Редактировать
            </Button>
            {isActive && (
              <>
                <Button onClick={() => setAction('MOVE')}>Переселить</Button>
                <Button danger onClick={() => setAction('EVICT')}>
                  Выселить
                </Button>
              </>
            )}
            <Button onClick={() => navigate(-1)}>Назад</Button>
          </Space>
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card title="Профиль">
            <Descriptions column={1} size="small" labelStyle={{ width: 200 }}>
              <Descriptions.Item label="ID жильца">
                <Tooltip title={resident.id}>{shortId(resident.id)}</Tooltip>
              </Descriptions.Item>
              <Descriptions.Item label="ID пользователя">
                <Tooltip title={resident.userId}>{shortId(resident.userId)}</Tooltip>
              </Descriptions.Item>
              <Descriptions.Item label="Категория">
                {RESIDENT_KIND_LABELS[resident.kind]}
              </Descriptions.Item>
              <Descriptions.Item label="Факультет">{resident.faculty ?? '—'}</Descriptions.Item>
              <Descriptions.Item label="Группа">{resident.studyGroup ?? '—'}</Descriptions.Item>
              <Descriptions.Item label="Подразделение">
                {resident.department ?? '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Телефон">{resident.phone ?? '—'}</Descriptions.Item>
              <Descriptions.Item label="Контактная информация">
                <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                  {resident.contactInfo ?? '—'}
                </Paragraph>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Текущее размещение">
            <Descriptions column={1} size="small" labelStyle={{ width: 160 }}>
              <Descriptions.Item label="Комната">
                {resident.roomId ? (
                  <Button
                    type="link"
                    style={{ padding: 0 }}
                    onClick={() => navigate(`/rooms/${resident.roomId}`)}
                  >
                    {resident.roomNumber ?? shortId(resident.roomId)}
                  </Button>
                ) : (
                  '—'
                )}
              </Descriptions.Item>
              <Descriptions.Item label="Заселён">{formatDate(resident.enrolledAt)}</Descriptions.Item>
              <Descriptions.Item label="Выселен">
                {resident.evictedAt ? formatDate(resident.evictedAt) : '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Создан">
                {formatDateTime(resident.createdAt)}
              </Descriptions.Item>
              <Descriptions.Item label="Обновлён">
                {formatDateTime(resident.updatedAt)}
              </Descriptions.Item>
            </Descriptions>
          </Card>

          <Card title="История проживания" style={{ marginTop: 16 }}>
            {history && history.length > 0 ? (
              <Timeline
                items={history.map((h) => ({
                  children: (
                    <div>
                      <Text strong>
                        {h.roomNumber ?? shortId(h.roomId)}: {formatDate(h.movedInAt)}
                        {' — '}
                        {h.movedOutAt ? formatDate(h.movedOutAt) : 'сейчас'}
                      </Text>
                      {h.reason && <div style={{ color: 'rgba(0,0,0,0.45)' }}>{h.reason}</div>}
                    </div>
                  ),
                }))}
              />
            ) : (
              <Empty description="История пуста" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>

      <Modal
        open={action === 'EVICT'}
        onCancel={() => setAction(null)}
        title="Выселить жильца"
        okText="Выселить"
        okButtonProps={{ danger: true }}
        cancelText="Отмена"
        confirmLoading={evict.isPending}
        onOk={onEvict}
      >
        <Form layout="vertical">
          <Form.Item label="Дата выселения" required>
            <DatePicker
              style={{ width: '100%' }}
              format="DD.MM.YYYY"
              value={dayjs(evictAt)}
              onChange={(d) => setEvictAt(d ? d.format('YYYY-MM-DD') : '')}
            />
          </Form.Item>
          <Form.Item label="Причина">
            <Input.TextArea
              rows={3}
              value={evictReason}
              onChange={(e) => setEvictReason(e.target.value)}
              maxLength={512}
              showCount
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={action === 'MOVE'}
        onCancel={() => setAction(null)}
        title="Переселить жильца"
        okText="Переселить"
        cancelText="Отмена"
        confirmLoading={move.isPending}
        onOk={onMove}
        okButtonProps={{ disabled: !moveRoomId }}
      >
        <Form layout="vertical">
          <Form.Item label="Новая комната" required>
            <Select
              showSearch
              optionFilterProp="label"
              value={moveRoomId}
              onChange={setMoveRoomId}
              options={(rooms?.content ?? []).map((r) => ({
                value: r.id,
                label: `${r.number} (этаж ${r.floor}, мест ${r.capacity - r.occupied}/${r.capacity})`,
              }))}
            />
          </Form.Item>
          <Form.Item label="Дата переселения" required>
            <DatePicker
              style={{ width: '100%' }}
              format="DD.MM.YYYY"
              value={dayjs(moveAt)}
              onChange={(d) => setMoveAt(d ? d.format('YYYY-MM-DD') : '')}
            />
          </Form.Item>
          <Form.Item label="Причина">
            <Input.TextArea
              rows={3}
              value={moveReason}
              onChange={(e) => setMoveReason(e.target.value)}
              maxLength={512}
              showCount
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={action === 'EDIT'}
        onCancel={() => setAction(null)}
        title="Редактировать данные жильца"
        okText="Сохранить"
        cancelText="Отмена"
        confirmLoading={update.isPending}
        onOk={onEdit}
      >
        <Form layout="vertical">
          <Form.Item label="Факультет">
            <Input
              value={edit.faculty}
              onChange={(e) => setEdit({ ...edit, faculty: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Группа">
            <Input
              value={edit.studyGroup}
              onChange={(e) => setEdit({ ...edit, studyGroup: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Подразделение">
            <Input
              value={edit.department}
              onChange={(e) => setEdit({ ...edit, department: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Телефон">
            <Input
              value={edit.phone}
              onChange={(e) => setEdit({ ...edit, phone: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Контактная информация">
            <Input.TextArea
              rows={3}
              value={edit.contactInfo}
              onChange={(e) => setEdit({ ...edit, contactInfo: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
