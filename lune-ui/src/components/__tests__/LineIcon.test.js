import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LineIcon from '../LineIcon.vue'

describe('LineIcon', () => {
  it('renders an svg with paths for a known icon', () => {
    const wrapper = mount(LineIcon, { props: { name: 'home' } })
    expect(wrapper.find('svg.line-icon').exists()).toBe(true)
    expect(wrapper.findAll('path').length).toBeGreaterThan(0)
  })

  it('falls back to a circle for an unknown icon name', () => {
    const wrapper = mount(LineIcon, { props: { name: 'does-not-exist' } })
    expect(wrapper.findAll('path').length).toBe(0)
    expect(wrapper.findAll('circle').length).toBe(1)
  })

  it('applies size and strokeWidth props', () => {
    const wrapper = mount(LineIcon, { props: { name: 'home', size: 32, strokeWidth: 2 } })
    const svg = wrapper.find('svg')
    expect(svg.attributes('width')).toBe('32')
    expect(svg.attributes('stroke-width')).toBe('2')
  })
})
