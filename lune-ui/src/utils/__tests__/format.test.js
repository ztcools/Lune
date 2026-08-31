import { describe, it, expect } from 'vitest'
import { safeJsonParse, nl2br, truncate } from '../format'

describe('safeJsonParse', () => {
  it('parses valid JSON', () => {
    expect(safeJsonParse('{"a":1}', null)).toEqual({ a: 1 })
  })

  it('returns fallback on invalid JSON', () => {
    expect(safeJsonParse('not-json', [])).toEqual([])
  })

  it('returns fallback on empty input', () => {
    expect(safeJsonParse('', 'default')).toBe('default')
    expect(safeJsonParse(null, 'default')).toBe('default')
  })
})

describe('nl2br', () => {
  it('converts newlines to <br/>', () => {
    expect(nl2br('a\nb')).toBe('a<br/>b')
  })

  it('returns empty for empty input', () => {
    expect(nl2br('')).toBe('')
    expect(nl2br(null)).toBe('')
  })
})

describe('truncate', () => {
  it('keeps short text intact', () => {
    expect(truncate('hello', 10)).toBe('hello')
  })

  it('truncates long text with suffix', () => {
    expect(truncate('abcdefghij', 5)).toBe('abcde...')
  })

  it('handles null', () => {
    expect(truncate(null, 5)).toBe('')
  })
})
