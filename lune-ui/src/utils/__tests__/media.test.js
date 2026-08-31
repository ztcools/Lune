import { describe, it, expect } from 'vitest'
import { parseMedia, extractImageUrls } from '../media'

describe('parseMedia', () => {
  it('parses string array into image objects', () => {
    expect(parseMedia('["a.jpg","b.png"]')).toEqual([
      { type: 'image', url: 'a.jpg' },
      { type: 'image', url: 'b.png' }
    ])
  })

  it('parses object array keeping type', () => {
    expect(parseMedia('[{"type":"video","url":"v.mp4"}]')).toEqual([
      { type: 'video', url: 'v.mp4' }
    ])
  })

  it('returns empty for invalid JSON', () => {
    expect(parseMedia('nope')).toEqual([])
  })

  it('returns empty for null', () => {
    expect(parseMedia(null)).toEqual([])
  })

  it('drops entries without url', () => {
    expect(parseMedia('[{"type":"image"}, "ok.jpg"]')).toEqual([
      { type: 'image', url: 'ok.jpg' }
    ])
  })
})

describe('extractImageUrls', () => {
  it('extracts only image urls', () => {
    const media = [
      { type: 'image', url: 'a.jpg' },
      { type: 'video', url: 'b.mp4' },
      { type: 'image', url: 'c.png' }
    ]
    expect(extractImageUrls(media)).toEqual(['a.jpg', 'c.png'])
  })

  it('returns empty for non-array', () => {
    expect(extractImageUrls(null)).toEqual([])
  })
})
