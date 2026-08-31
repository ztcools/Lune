import { describe, it, expect } from 'vitest'
import { processImage, responsiveProfile } from '../imageUrl'

describe('processImage', () => {
  it('leaves local paths untouched', () => {
    expect(processImage('/upload/a.jpg', 'thumb')).toBe('/upload/a.jpg')
    expect(processImage('/media/b.jpg', 'thumb')).toBe('/media/b.jpg')
    expect(processImage('data:image/png;base64,xxx', 'thumb')).toBe('data:image/png;base64,xxx')
  })

  it('appends processing params to remote urls', () => {
    const out = processImage('https://cdn.example.com/a.jpg', 'thumb')
    expect(out).toContain('https://cdn.example.com/a.jpg')
    expect(out).toContain('imageMogr2')
  })

  it('uses & separator when url already has query', () => {
    const out = processImage('https://cdn.example.com/a.jpg?v=1', 'thumb')
    expect(out).toContain('?v=1&imageMogr2')
  })

  it('returns empty for falsy url', () => {
    expect(processImage('')).toBe('')
    expect(processImage(null)).toBe('')
  })
})

describe('responsiveProfile', () => {
  it('returns desktop profile on desktop', () => {
    expect(responsiveProfile(false, 'hero')).toBe('hero')
  })

  it('appends Mobile suffix on mobile', () => {
    expect(responsiveProfile(true, 'hero')).toBe('heroMobile')
  })

  it('uses explicit mobile profile when provided', () => {
    expect(responsiveProfile(true, 'hero', 'customMobile')).toBe('customMobile')
  })
})
