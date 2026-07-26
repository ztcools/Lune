import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  sendCode: (email) => request.post('/auth/send-code', { email }),
  logout: () => request.post('/auth/logout')
}

export const userProfileApi = {
  get: () => request.get('/user/profile'),
  getPublic: (userId) => request.get(`/user/profile/${userId}`),
  update: (data) => request.put('/user/profile', data),
  changePassword: (data) => request.put('/user/password', data),
  sendDeleteCode: () => request.post('/user/send-delete-code'),
  deleteAccount: (code) => request.delete('/user/account', { data: { code } })
}

export const articleApi = {
  list: (params) => request.get('/articles', { params }),
  getById: (id) => request.get(`/articles/${id}`),
  create: (data) => request.post('/admin/articles', data),
  update: (id, data) => request.put(`/admin/articles/${id}`, data),
  delete: (id) => request.delete(`/admin/articles/${id}`)
}

export const categoryApi = {
  list: (type) => request.get('/categories', { params: { type } }),
  listAll: () => request.get('/admin/categories'),
  create: (data) => request.post('/admin/categories', data),
  update: (id, data) => request.put(`/admin/categories/${id}`, data),
  delete: (id) => request.delete(`/admin/categories/${id}`)
}

export const tagApi = {
  list: () => request.get('/tags'),
  listAll: () => request.get('/admin/tags'),
  create: (data) => request.post('/admin/tags', data),
  delete: (id) => request.delete(`/admin/tags/${id}`)
}

export const commentApi = {
  list: (params) => request.get('/comments', { params }),
  listAll: (params) => request.get('/admin/comments', { params }),
  create: (data) => request.post('/comments', data),
  delete: (id) => request.delete(`/admin/comments/${id}`)
}

export const treeHoleApi = {
  list: (params) => request.get('/treeholes', { params }),
  create: (data) => request.post('/treeholes', data),
  listAll: (params) => request.get('/admin/treeholes', { params }),
  delete: (id) => request.delete(`/admin/treeholes/${id}`)
}

export const essayApi = {
  list: (params) => request.get('/essays', { params }),
  getById: (id) => request.get(`/essays/${id}`),
  create: (data) => request.post('/admin/essays', data),
  update: (id, data) => request.put(`/admin/essays/${id}`, data),
  delete: (id) => request.delete(`/admin/essays/${id}`)
}

export const recordApi = {
  list: (params) => request.get('/records', { params }),
  create: (data) => request.post('/admin/records', data),
  update: (id, data) => request.put(`/admin/records/${id}`, data),
  delete: (id) => request.delete(`/admin/records/${id}`)
}

export const familyApi = {
  list: () => request.get('/family'),
  listAll: () => request.get('/family'),
  create: (data) => request.post('/admin/family', data),
  update: (id, data) => request.put(`/admin/family/${id}`, data),
  delete: (id) => request.delete(`/admin/family/${id}`)
}

export const diaryApi = {
  list: (params) => request.get('/diaries', { params }),
  getById: (id) => request.get(`/diaries/${id}`),
  adminList: (params) => request.get('/admin/diaries', { params }),
  create: (data) => request.post('/admin/diaries', data),
  update: (id, data) => request.put(`/admin/diaries/${id}`, data),
  delete: (id) => request.delete(`/admin/diaries/${id}`)
}

export const siteConfigApi = {
  getPublic: () => request.get('/site-config/public'),
  listAll: () => request.get('/admin/site-configs'),
  save: (data) => request.post('/admin/site-configs', data),
  delete: (id) => request.delete(`/admin/site-configs/${id}`)
}

export const resourceApi = {
  list: (params) => request.get('/admin/resources', { params }),
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/admin/resources/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  importUrl: (url) => request.post('/admin/resources/import-url', { url }),
  delete: (id) => request.delete(`/admin/resources/${id}`)
}

export const userApi = {
  list: (params) => request.get('/admin/users', { params }),
  update: (id, data) => request.put(`/admin/users/${id}`, data),
  delete: (id) => request.delete(`/admin/users/${id}`),
  updateRole: (id, role) => request.put(`/admin/users/${id}/role`, null, { params: { role } })
}
