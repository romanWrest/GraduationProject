import { describe, it, expect } from 'vitest';
import { formatBytes, formatDate, formatDateTime, shortId } from './format';

describe('format utils', () => {
  it('formatDateTime: returns dash for empty input', () => {
    expect(formatDateTime(null)).toBe('—');
    expect(formatDateTime(undefined)).toBe('—');
  });

  it('formatDateTime: formats valid ISO string', () => {
    const result = formatDateTime('2026-01-15T12:34:00Z');
    expect(result).toMatch(/^\d{2}\.\d{2}\.\d{4} \d{2}:\d{2}$/);
  });

  it('formatDate: returns dash for null', () => {
    expect(formatDate(null)).toBe('—');
  });

  it('shortId: returns first 8 chars', () => {
    expect(shortId('abcdef12-3456-7890-abcd-ef1234567890')).toBe('abcdef12');
  });

  it('shortId: returns dash for empty', () => {
    expect(shortId(undefined)).toBe('—');
    expect(shortId('')).toBe('—');
  });

  it('formatBytes: 0 → 0 Б', () => {
    expect(formatBytes(0)).toBe('0 Б');
  });

  it('formatBytes: 512 → 512 Б', () => {
    expect(formatBytes(512)).toBe('512 Б');
  });

  it('formatBytes: 2048 → 2.0 КБ', () => {
    expect(formatBytes(2048)).toBe('2.0 КБ');
  });

  it('formatBytes: 1.5 МБ', () => {
    expect(formatBytes(1.5 * 1024 * 1024)).toBe('1.5 МБ');
  });
});
