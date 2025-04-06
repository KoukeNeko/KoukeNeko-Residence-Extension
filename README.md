# KoukeNeko Residence Extension

一個 Minecraft 伺服器插件，為 Residence 插件提供視覺化選擇反饋功能，讓玩家在選擇領地時更加直觀。

## 功能特色

- **視覺化 BossBar 顯示**：在玩家選擇領地時，自動顯示區域大小和花費
- **自動隱藏**：BossBar 會在指定時間後自動隱藏，不影響視線
- **高效能設計**：使用事件驅動和防抖機制，最大限度減少資源消耗
- **完全可配置**：所有功能都可以通過配置文件自定義

## 依賴

- [Residence](https://www.spigotmc.org/resources/residence.11480/) (版本 5.0.0+)
- [CMILib](https://www.spigotmc.org/resources/cmilib.87610/) (Residence 的依賴)

## 安裝

1. 下載 KoukeNeko-Residence-Extension 的最新版本
2. 放入伺服器的 `plugins` 目錄
3. 重啟伺服器或使用插件管理器加載插件
4. 編輯 `plugins/KoukeNeko-Residence-Extension/config.yml` 進行個性化設置

## 配置

插件的配置文件 `config.yml` 包含以下選項：

```yaml
prefix: "&7[&e&l領地&6&l系統&7]&f" # 訊息前綴

# 領地選擇 BossBar 設定
boss-bar:
  enabled: true # 是否啟用 BossBar 功能
  title: "&e領地選擇: &b{size} &6區塊 &7- &a花費: &2${cost}" # BossBar 標題格式
  color: YELLOW # 顏色: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
  style: SOLID # 樣式: SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
  auto-hide: true # 是否自動隱藏 BossBar
  display-seconds: 5 # BossBar 顯示秒數
  
# 訊息設定
messages:
  show-selection-updated: true # 是否在聊天中顯示選擇更新的訊息
  selection-updated: "&e已更新選擇範圍，大小: &b{size} &e區塊，花費: &a${cost}"
  selection-too-large: "&c選擇範圍過大！最大允許: &e{max} &c區塊"
  no-permission: "&c你沒有權限執行此操作！"
```

### BossBar 配置

- **enabled**: 設為 `true` 啟用 BossBar 功能，`false` 禁用
- **title**: 設置 BossBar 的標題格式，支持 `{size}` 和 `{cost}` 變量
- **color**: BossBar 的顏色
- **style**: BossBar 的樣式（實心或分段）
- **auto-hide**: 設為 `true` 讓 BossBar 自動隱藏，`false` 則一直顯示
- **display-seconds**: BossBar 顯示的秒數（僅當 auto-hide 為 true 時有效）

### 訊息配置

- **show-selection-updated**: 是否在聊天中顯示選擇範圍更新訊息
- **selection-updated**: 選擇範圍更新時的訊息格式
- **selection-too-large**: 選擇範圍過大時的錯誤訊息
- **no-permission**: 權限不足時的錯誤訊息

## 命令

- **/knres reload** - 重新載入插件配置
  - 權限: `koukeneko.admin`
- **/kntest bossbar** - 測試 BossBar 顯示
  - 權限: `koukeneko.admin`

## 權限

- **koukeneko.admin** - 允許使用所有管理命令

## 性能優化

本插件使用以下技術來確保高效運行：

- **事件驅動更新**：只在選擇發生變化時才更新 BossBar
- **防抖機制**：避免短時間內重複更新
- **自動資源清理**：玩家退出或插件卸載時自動清理資源

## 問題報告

如果你發現任何問題或有功能建議，請在 GitHub 上提交 Issue。

## 授權

此插件採用 MIT 授權。
