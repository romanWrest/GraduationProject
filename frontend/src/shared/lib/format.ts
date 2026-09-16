import dayjs from 'dayjs';

export function formatDateTime(input: string | Date | null | undefined): string {
  if (!input) return '—';
  const d = dayjs(input);
  return d.isValid() ? d.format('DD.MM.YYYY HH:mm') : '—';
}

export function formatDate(input: string | Date | null | undefined): string {
  if (!input) return '—';
  const d = dayjs(input);
  return d.isValid() ? d.format('DD.MM.YYYY') : '—';
}

export function shortId(id: string | null | undefined): string {
  if (!id) return '—';
  return id.slice(0, 8);
}

export function formatBytes(bytes: number): string {
  if (!bytes) return '0 Б';
  const units = ['Б', 'КБ', 'МБ', 'ГБ'];
  let value = bytes;
  let unit = 0;
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024;
    unit += 1;
  }
  return `${value.toFixed(unit ? 1 : 0)} ${units[unit]}`;
}
