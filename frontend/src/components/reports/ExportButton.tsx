import { App, Button, Dropdown } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { downloadBlob, exportReport } from '@/features/reports/api';
import type { ExportFormat, ReportName } from '@/features/reports/types';
import { handleApiError } from '@/shared/lib/handleApiError';

interface Props {
  name: ReportName;
  params: Record<string, string | undefined>;
  disabled?: boolean;
}

export function ExportButton({ name, params, disabled }: Props) {
  const { message } = App.useApp();

  const onExport = async (format: ExportFormat) => {
    try {
      const { blob, filename } = await exportReport(name, format, params);
      downloadBlob(blob, filename);
      message.success(`Файл ${filename} скачан`);
    } catch (e) {
      handleApiError(e);
    }
  };

  return (
    <Dropdown
      disabled={disabled}
      menu={{
        items: [
          { key: 'csv', label: 'CSV', onClick: () => onExport('csv') },
          { key: 'xlsx', label: 'XLSX (Excel)', onClick: () => onExport('xlsx') },
          { key: 'pdf', label: 'PDF', onClick: () => onExport('pdf') },
        ],
      }}
    >
      <Button icon={<DownloadOutlined />} disabled={disabled}>
        Экспорт
      </Button>
    </Dropdown>
  );
}
