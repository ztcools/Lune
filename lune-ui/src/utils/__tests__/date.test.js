import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { formatDate, formatFullDate, formatLocaleDate, formatRelative } from '../date'

describe('formatDate', () => {
  it('formats a Date object as YYYY-MM-DD', () => {
    expect(formatDate(new Date(2024, 7, 8, 10, 0, 0))).toBe('2024-08-08')
  })

  it('returns empty for falsy input', () => {
    expect(formatDate('')).toBe('')
    expect(formatDate(null)).toBe('')
  })
})

describe('formatFullDate / formatLocaleDate', () => {
  it('return non-empty localized strings', () => {
    const d = new Date(2024, 7, 8)
    expect(formatFullDate(d).length).toBeGreaterThan(0)
    expect(formatLocaleDate(d).length).toBeGreaterThan(0)
  })
})

describe('formatRelative', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date(2024, 7, 8, 12, 0, 0))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('returns 刚刚 within a minute', () => {
    expect(formatRelative(new Date(2024, 7, 8, 11, 59, 30))).toBe('刚刚')
  })

  it('returns minutes ago', () => {
    expect(formatRelative(new Date(2024, 7, 8, 11, 57, 0))).toBe('3分钟前')
  })

  it('returns hours ago', () => {
    expect(formatRelative(new Date(2024, 7, 8, 10, 0, 0))).toBe('2小时前')
  })

  it('returns days ago', () => {
    expect(formatRelative(new Date(2024, 7, 3, 12, 0, 0))).toBe('5天前')
  })

  it('returns months ago', () => {
    expect(formatRelative(new Date(2024, 5, 8, 12, 0, 0))).toBe('2个月前')
  })

  it('returns empty for falsy input', () => {
    expect(formatRelative('')).toBe('')
    expect(formatRelative(null)).toBe('')
  })
})
