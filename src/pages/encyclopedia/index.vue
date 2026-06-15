<template>
  <view class="enc-page">
    <!-- 分类筛选 -->
    <view class="filter-row">
      <view
        v-for="cat in categories"
        :key="cat.id"
        class="filter-btn"
        :class="{ active: currentCat === cat.id }"
        @click="currentCat = cat.id"
      >{{ cat.label }}</view>
    </view>

    <!-- 统计 -->
    <view class="stats-bar">
      <text class="stats-text">已解锁 {{ unlockedCount }} / {{ filteredCards.length }} 张</text>
      <view class="stats-track">
        <view class="stats-fill" :style="{ width: (unlockedCount / filteredCards.length * 100) + '%' }"></view>
      </view>
    </view>

    <!-- 卡片网格 -->
    <view class="card-grid">
      <view
        v-for="card in filteredCards"
        :key="card.id"
        class="enc-card"
        :class="{ locked: !isUnlocked(card.id), [card.type]: true }"
        @click="openCard(card)"
      >
        <text class="card-emoji">{{ isUnlocked(card.id) ? card.emoji : '🔒' }}</text>
        <text class="card-name">{{ isUnlocked(card.id) ? card.name : '???' }}</text>
        <text class="card-cat">{{ isUnlocked(card.id) ? card.catLabel : card.type === 'astronomy' ? '天文' : card.type === 'history' ? '历史' : '昆虫' }}</text>
        <view v-if="isUnlocked(card.id)" class="unlocked-badge">已解锁</view>
      </view>
    </view>

    <!-- 详情弹窗 -->
    <view v-if="detail" class="modal-mask" @click.self="detail = null">
      <view class="detail-modal">
        <text class="detail-emoji">{{ detail.emoji }}</text>
        <text class="detail-name">{{ detail.name }}</text>
        <view class="detail-tag" :class="detail.type">{{ detail.catLabel }}</view>
        <text class="detail-desc">{{ detail.desc }}</text>
        <text class="detail-fact">💡 {{ detail.fact }}</text>
        <button class="close-btn" @click="detail = null">关闭</button>
      </view>
    </view>

    <view style="height: 150rpx;"></view>
    <CustomTabBar current="encyclopedia" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { gameStore } from '@/store/gameStore'
import CustomTabBar from '@/components/CustomTabBar.vue'

const currentCat = ref('all')
const detail     = ref(null)

const categories = [
  { id: 'all',       label: '全部' },
  { id: 'astronomy', label: '🌟 天文' },
  { id: 'history',   label: '🏺 历史' },
  { id: 'insect',    label: '🦋 昆虫' }
]

const allCards = [
  {
    id: 'orion', type: 'astronomy', catLabel: '天文',
    emoji: '⭐', name: '猎户座',
    desc: '猎户座是冬季最壮观的星座，包含参宿四（红超巨星）和参宿七（蓝超巨星）等著名亮星。',
    fact: '猎户座腰带三星连线延伸可找到大犬座最亮星——天狼星。'
  },
  {
    id: 'bigdipper', type: 'astronomy', catLabel: '天文',
    emoji: '🌠', name: '北斗七星',
    desc: '北斗七星属大熊座，由天枢、天璇、天玑、天权、玉衡、开阳、摇光七颗星组成，形如北斗。',
    fact: '开阳星旁有一颗伴星"辅"，视力好的人可以肉眼分辨它们。'
  },
  {
    id: 'cassiopeia', type: 'astronomy', catLabel: '天文',
    emoji: '✨', name: '仙后座',
    desc: '仙后座五颗亮星排列成W形，是北方天空的重要地标，全年不落地平线，四季可见。',
    fact: '仙后座与北斗七星分别位于北极星两侧，可相互辅助寻找北极星。'
  },
  {
    id: 'bronze', type: 'history', catLabel: '历史',
    emoji: '🏺', name: '青花瓷',
    desc: '青花瓷始于唐代，成熟于元代，以白底蓝花为特征。使用钴料在坯体上绘画，施透明釉高温烧制。',
    fact: '明代永乐、宣德年间的青花瓷被认为是青花瓷的黄金时代。'
  },
  {
    id: 'ceramic', type: 'history', catLabel: '历史',
    emoji: '🗿', name: '兵马俑',
    desc: '秦始皇陵兵马俑1974年发现于陕西临潼，目前已出土陶俑、陶马逾8000件，每件面貌各异。',
    fact: '兵马俑原本有彩绘，出土后因与空气接触迅速氧化而褪色。'
  },
  {
    id: 'jade', type: 'history', catLabel: '历史',
    emoji: '🪙', name: '铜镜',
    desc: '铜镜是古代铜制照面用具，以青铜铸造，正面光亮可照人，背面有纹饰，汉唐铜镜最为精美。',
    fact: '古人认为铜镜有辟邪功效，常随葬或悬挂于门前。'
  },
  {
    id: 'butterfly', type: 'insect', catLabel: '昆虫',
    emoji: '🦋', name: '蝴蝶',
    desc: '蝴蝶是鳞翅目昆虫，翅膀上布满细小鳞粉，经历完全变态发育：卵→幼虫→蛹→成虫。',
    fact: '蝴蝶用足上的味觉感受器来"品尝"食物，落在叶子上就能判断是否可食。'
  },
  {
    id: 'bee', type: 'insect', catLabel: '昆虫',
    emoji: '🐝', name: '蜜蜂',
    desc: '蜜蜂是膜翅目蜜蜂科社会性昆虫，一个蜂群由一只蜂王、数百只雄蜂和数万只工蜂组成。',
    fact: '蜜蜂通过"8字舞"向同伴传递花蜜位置的方向和距离信息。'
  },
  {
    id: 'ant', type: 'insect', catLabel: '昆虫',
    emoji: '🐜', name: '蚂蚁',
    desc: '蚂蚁是膜翅目蚁科社会性昆虫，能搬运超过自身体重50倍的物体，并通过信息素进行通讯。',
    fact: '全球蚂蚁总重量约与全球人类总重量相当。'
  }
]

const filteredCards = computed(() =>
  currentCat.value === 'all' ? allCards : allCards.filter(c => c.type === currentCat.value)
)

const unlockedCount = computed(() =>
  filteredCards.value.filter(c => isUnlocked(c.id)).length
)

function isUnlocked(id) {
  return gameStore.state.encyclopedia[id] === true
}

function openCard(card) {
  if (!isUnlocked(card.id)) {
    uni.showToast({ title: '完成对应游戏后解锁', icon: 'none', duration: 1500 })
    return
  }
  detail.value = card
}
</script>

<style scoped>
.enc-page {
  background: #F0F9FF;
  min-height: 100vh;
}

/* 筛选 */
.filter-row {
  display: flex;
  flex-direction: row;
  padding: 24rpx 20rpx 16rpx;
  gap: 12rpx;
}

.filter-btn {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: 14rpx;
  font-size: 24rpx;
  color: #666;
  background: #fff;
  border: 2rpx solid #e0e0e0;
  transition: all 0.2s;
}

.filter-btn.active {
  background: #4ECDC4;
  color: #fff;
  border-color: #4ECDC4;
  font-weight: bold;
}

/* 统计 */
.stats-bar {
  margin: 0 24rpx 20rpx;
}

.stats-text {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.stats-track {
  background: #e0f2f1;
  height: 10rpx;
  border-radius: 5rpx;
  overflow: hidden;
}

.stats-fill {
  height: 100%;
  background: linear-gradient(90deg, #4ECDC4, #26C6DA);
  border-radius: 5rpx;
  transition: width 0.4s ease;
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  padding: 0 20rpx;
}

.enc-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx 12rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  position: relative;
  cursor: pointer;
  transition: transform 0.15s;
}

.enc-card:active { transform: scale(0.95); }

.enc-card.locked {
  background: #f5f5f5;
  opacity: 0.7;
}

.enc-card.astronomy { border-top: 6rpx solid #1A1A3E; }
.enc-card.history   { border-top: 6rpx solid #6B3A2A; }
.enc-card.insect    { border-top: 6rpx solid #2E7D32; }

.card-emoji { display: block; font-size: 60rpx; margin-bottom: 10rpx; }
.card-name  { display: block; font-size: 26rpx; font-weight: bold; color: #333; margin-bottom: 6rpx; }
.card-cat   { display: block; font-size: 20rpx; color: #aaa; }

.unlocked-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  background: #4ECDC4;
  color: #fff;
  font-size: 18rpx;
  padding: 2rpx 10rpx;
  border-radius: 10rpx;
}

/* 详情弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.detail-modal {
  background: #fff;
  border-radius: 36rpx;
  padding: 50rpx 40rpx;
  margin: 40rpx;
  text-align: center;
  max-height: 80vh;
  overflow-y: auto;
}

.detail-emoji { display: block; font-size: 100rpx; margin-bottom: 16rpx; }
.detail-name  { display: block; font-size: 44rpx; font-weight: bold; color: #222; margin-bottom: 12rpx; }

.detail-tag {
  display: inline-block;
  border-radius: 20rpx;
  padding: 6rpx 20rpx;
  font-size: 22rpx;
  color: #fff;
  margin-bottom: 24rpx;
}

.detail-tag.astronomy { background: #1A1A3E; }
.detail-tag.history   { background: #6B3A2A; }
.detail-tag.insect    { background: #2E7D32; }

.detail-desc {
  display: block;
  font-size: 28rpx;
  color: #444;
  line-height: 1.8;
  text-align: left;
  margin-bottom: 20rpx;
}

.detail-fact {
  display: block;
  font-size: 26rpx;
  color: #4ECDC4;
  background: #E0F7FA;
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  line-height: 1.7;
  text-align: left;
  margin-bottom: 30rpx;
}

.close-btn {
  background: #4ECDC4;
  color: #fff;
  border-radius: 50rpx;
  font-size: 30rpx;
  padding: 18rpx 60rpx;
  border: none;
}
</style>
