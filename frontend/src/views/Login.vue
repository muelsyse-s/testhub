<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-mark">TH</div>
        <div>
          <div class="brand-title">TestHub</div>
          <div class="brand-sub">测试资产管理系统</div>
        </div>
      </div>

      <div class="headline">
        让测试资产<br />可沉淀、可复用、可度量
      </div>
      <div class="desc">
        从需求拆解测试点，到用例设计、计划编排、执行记录、缺陷闭环，
        再到接口脚本自动化回归，一个平台串起测试全流程。
      </div>

      <div class="features">
        <div class="feature" v-for="f in features" :key="f.title">
          <el-icon :size="18"><component :is="f.icon" /></el-icon>
          <div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <div class="card-title">登录</div>
        <div class="card-sub">请使用测试平台账号登录系统</div>

        <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="onSubmit">
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              show-password
              placeholder="密码"
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="submit" @click="onSubmit">
            登录
          </el-button>
        </el-form>

        <div class="tip">
          <span>演示账号：</span>
          <el-tag size="small" effect="plain" @click="fill('wtb')" style="cursor: pointer">wtb / 123456</el-tag>
          <el-tag size="small" effect="plain" @click="fill('lisi')" style="cursor: pointer">lisi / 123456</el-tag>
        </div>
      </div>
      <div class="copyright">© 2026 TestHub · 综合实践训练</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: 'wtb', password: '123456' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const features = [
  { icon: 'Aim', title: '测试点驱动', desc: '需求逐条拆解为测试点，避免漏测' },
  { icon: 'Document', title: '用例资产化', desc: '用例模板统一、可复制、可追溯变更' },
  { icon: 'Cpu', title: '接口自动化', desc: 'HTTP + 断言 + 变量传递，一键回归' }
]

function fill(username: string) {
  form.username = username
  form.password = '123456'
}

async function onSubmit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form.username, form.password)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  background: #fff;
}

.login-left {
  width: 55%;
  background:
    radial-gradient(circle at 15% 20%, rgba(99, 102, 241, 0.35), transparent 45%),
    radial-gradient(circle at 85% 75%, rgba(56, 189, 248, 0.28), transparent 45%),
    linear-gradient(150deg, #1e2233 0%, #272c44 55%, #202748 100%);
  color: #fff;
  padding: 54px 64px;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(135deg, #6366f1, #4338ca);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.brand-title {
  font-size: 19px;
  font-weight: 600;
}

.brand-sub {
  font-size: 11.5px;
  color: #9aa1b8;
}

.headline {
  margin-top: 92px;
  font-size: 38px;
  line-height: 1.35;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.desc {
  margin-top: 20px;
  font-size: 14px;
  line-height: 1.9;
  color: #b8bfd4;
  max-width: 470px;
}

.features {
  margin-top: auto;
  display: flex;
  gap: 26px;
}

.feature {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  color: #c8cee0;
}

.feature-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #fff;
}

.feature-desc {
  font-size: 11.5px;
  color: #98a0b8;
  margin-top: 3px;
}

.login-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-card {
  width: 372px;
}

.card-title {
  font-size: 25px;
  font-weight: 700;
}

.card-sub {
  margin: 8px 0 26px;
  font-size: 13px;
  color: #8a90a2;
}

.submit {
  width: 100%;
  margin-top: 6px;
  letter-spacing: 2px;
}

.tip {
  margin-top: 22px;
  font-size: 12px;
  color: #8a90a2;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.copyright {
  position: absolute;
  bottom: 26px;
  font-size: 11.5px;
  color: #b0b6c4;
}
</style>
