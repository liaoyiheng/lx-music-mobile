import { useMemo, useState, useEffect, memo } from 'react'
import { View, ScrollView } from 'react-native'

import { compareVer, sizeFormate } from '@/utils'

import Button from '@/components/common/Button'
import { updateApp } from '@/utils/version'
import { createStyle } from '@/utils/tools'
import { type VersionInfo } from '@/store/version/state'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useVersionDownloadProgressUpdated, useVersionInfo, useVersionInfoIgnoreVersionUpdated } from '@/store/version/hook'
import ModalContent from './ModalContent'
import { checkUpdate, downloadUpdate, hideModal, setIgnoreVersion } from '@/core/version'
import { useApple, fontWeight, hairline, radius } from '@/theme/apple'

const VersionItem = ({ version, desc }: VersionInfo) => {
  const apple = useApple()
  return (
    <View style={styles.versionItem}>
      <Text style={styles.versionTag} size={13} color={apple.label}>v{version}</Text>
      <Text selectable style={styles.desc} size={13} color={apple.secondaryLabel}>{desc}</Text>
    </View>
  )
}

const Content = memo(({ title, newVersionInfo, isLatest }: {
  title: string
  newVersionInfo: VersionInfo | null
  isLatest: boolean
}) => {
  const apple = useApple()
  const t = useI18n()

  const history = useMemo(() => {
    if (isLatest || !newVersionInfo?.history) return []
    const arr: LX.VersionInfo[] = []
    for (const ver of newVersionInfo.history) {
      if (ver.version === currentVer) break
      if (compareVer(currentVer, ver.version) < 0) arr.push(ver)
    }
    return arr
  }, [newVersionInfo, isLatest])

  return (
    <View style={styles.main}>
      <Text style={styles.title} size={17} color={apple.label}>{title}</Text>
      <ScrollView style={styles.content} keyboardShouldPersistTaps={'always'}>
        <View style={[styles.metaCard, { backgroundColor: apple.fill, borderColor: apple.separator }]}>
          <View style={styles.metaRow}>
            <Text size={13} color={apple.secondaryLabel}>{t('version_label_latest_ver')}</Text>
            <Text
              size={13}
              style={styles.metaValue}
              color={isLatest ? apple.label : apple.accent}
              numberOfLines={1}
            >
              {newVersionInfo?.version || '-'}
            </Text>
          </View>
          <View style={[styles.metaDivider, { backgroundColor: apple.separator }]} />
          <View style={styles.metaRow}>
            <Text size={13} color={apple.secondaryLabel}>{t('version_label_current_ver')}</Text>
            <Text size={13} style={styles.metaValue} color={apple.label} numberOfLines={1}>
              {currentVer}
            </Text>
          </View>
        </View>
        {
          newVersionInfo?.desc
            ? (
                <View style={styles.section}>
                  <Text style={styles.sectionTitle} size={13} color={apple.label}>{t('version_label_change_log')}</Text>
                  <Text selectable style={styles.desc} size={13} color={apple.secondaryLabel}>{newVersionInfo.desc}</Text>
                </View>
              )
            : null
        }
        {
          history.length
            ? (
                <View style={styles.section}>
                  <Text style={styles.sectionTitle} size={13} color={apple.label}>{t('version_label_history')}</Text>
                  <View style={styles.historyList}>
                    {history.map((item, index) => <VersionItem key={index} version={item.version} desc={item.desc} />)}
                  </View>
                </View>
              )
            : null
        }
      </ScrollView>
    </View>
  )
})

const currentVer = process.versions.app
const VersionModal = ({ componentId }: { componentId: string }) => {
  const apple = useApple()
  const t = useI18n()
  const versionInfo = useVersionInfo()
  const progress = useVersionDownloadProgressUpdated()
  const ignoreVersion = useVersionInfoIgnoreVersionUpdated()
  const [ignoreBtn, setIgnoreBtn] = useState({ text: t('version_btn_ignore'), show: true, disabled: false })
  const [closeBtnText, setCloseBtnText] = useState(t('version_btn_close'))
  const [confirmBtn, setConfirmBtn] = useState({ text: '', show: true, disabled: false })
  const [title, setTitle] = useState('')
  const [tip, setTip] = useState('')

  useEffect(() => {
    let ignoreBtnConfig = { ...ignoreBtn }
    if (versionInfo.isLatest) {
      setTitle(t('version_title_latest'))
      setTip('')
      ignoreBtnConfig.show = false
      setConfirmBtn({ text: t('version_btn_new'), show: false, disabled: true })
      setCloseBtnText(t('version_btn_close'))
    } else if (versionInfo.isUnknown) {
      setTitle(t('version_title_unknown'))
      setTip(t('version_tip_unknown'))
      ignoreBtnConfig.show = false
      setConfirmBtn({ text: t('version_btn_failed'), show: true, disabled: false })
      setCloseBtnText(t('version_btn_close'))
    } else {
      switch (versionInfo.status) {
        case 'downloading':
          setTitle(t('version_title_new'))
          setTip(t('version_btn_downloading', {
            total: sizeFormate(progress.total),
            current: sizeFormate(progress.current),
            progress: progress.total ? (progress.current / progress.total * 100).toFixed(2) : '0',
          }))
          if (ignoreBtnConfig.show) ignoreBtnConfig.show = false
          if (!confirmBtn.disabled) setConfirmBtn({ text: t('version_btn_update'), show: true, disabled: true })
          setCloseBtnText(t('version_btn_min'))
          break
        case 'downloaded':
          setTitle(t('version_title_update'))
          setTip('')
          if (ignoreBtnConfig.show) ignoreBtnConfig.show = false
          setConfirmBtn({ text: t('version_btn_update'), show: true, disabled: false })
          setCloseBtnText(t('version_btn_close'))
          break
        case 'checking':
          setTitle(t('version_title_checking'))
          setTip('')
          ignoreBtnConfig.show = false
          setConfirmBtn({ text: t('version_btn_new'), show: false, disabled: true })
          setCloseBtnText(t('version_btn_close'))
          break
        case 'error':
          setTitle(t('version_title_failed'))
          setTip(t('version_tip_failed'))
          ignoreBtnConfig.show = true
          ignoreBtnConfig.disabled = false
          setConfirmBtn({ text: t('version_btn_failed'), show: true, disabled: false })
          setCloseBtnText(t('version_btn_close'))
          break
        default:
          setTitle(t('version_title_new'))
          setTip('')
          ignoreBtnConfig.show = true
          ignoreBtnConfig.disabled = false
          setConfirmBtn({ text: t('version_btn_new'), show: true, disabled: false })
          setCloseBtnText(t('version_btn_close'))
          break
      }
    }
    ignoreBtnConfig.text = t(ignoreVersion == versionInfo.newVersion?.version ? 'version_btn_ignore_cancel' : 'version_btn_ignore')
    setIgnoreBtn(ignoreBtnConfig)

  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [t, versionInfo, ignoreVersion, progress])

  const handleCancel = () => {
    hideModal(componentId)
  }
  const handleIgnore = () => {
    setIgnoreVersion(ignoreVersion != versionInfo.newVersion!.version ? versionInfo.newVersion!.version : null)
  }

  const handleConfirm = () => {
    if (versionInfo.isLatest || versionInfo.isUnknown) {
      void checkUpdate()
    } else if (versionInfo.status == 'downloaded') {
      void updateApp()
    } else if (versionInfo.status == 'idle' || versionInfo.status == 'error') {
      downloadUpdate()
    }
  }

  return (
    <ModalContent>
      <Content title={title} newVersionInfo={versionInfo.newVersion} isLatest={versionInfo.isLatest} />
      {tip.length ? <Text style={styles.tip} size={13} color={apple.accent}>{tip}</Text> : null}
      <View style={styles.btns}>
        {
          ignoreBtn.show
            ? (
                <Button
                  disabled={ignoreBtn.disabled}
                  activeOpacity={0.65}
                  style={[
                    styles.btn,
                    {
                      backgroundColor: apple.fill,
                      borderColor: apple.glassBorder,
                    },
                    ignoreBtn.disabled ? styles.btnDisabled : null,
                  ]}
                  onPress={handleIgnore}
                >
                  <Text size={14} color={apple.secondaryLabel} style={styles.btnText} numberOfLines={1}>{ignoreBtn.text}</Text>
                </Button>
              )
            : null
        }
        <Button
          activeOpacity={0.65}
          style={[
            styles.btn,
            {
              backgroundColor: apple.fill,
              borderColor: apple.glassBorder,
            },
          ]}
          onPress={handleCancel}
        >
          <Text size={14} color={apple.label} style={styles.btnText} numberOfLines={1}>{closeBtnText}</Text>
        </Button>
        {
          confirmBtn.show
            ? (
                <Button
                  disabled={confirmBtn.disabled}
                  activeOpacity={0.75}
                  style={[
                    styles.btn,
                    styles.primaryBtn,
                    {
                      backgroundColor: apple.accent,
                      borderColor: 'rgba(255, 255, 255, 0.34)',
                      shadowColor: apple.accent,
                    },
                    confirmBtn.disabled ? styles.btnDisabled : null,
                  ]}
                  onPress={handleConfirm}
                >
                  <Text size={14} color={apple.onAccent} style={styles.btnText} numberOfLines={1}>{confirmBtn.text}</Text>
                </Button>
              )
            : null
        }
      </View>
    </ModalContent>
  )
}

const styles = createStyle({
  main: {
    flexShrink: 1,
    paddingTop: 20,
    paddingHorizontal: 18,
    paddingBottom: 14,
  },
  content: {
    flexGrow: 0,
  },
  title: {
    textAlign: 'center',
    fontWeight: fontWeight.bold,
    letterSpacing: -0.2,
    marginBottom: 14,
  },
  metaCard: {
    borderRadius: radius.large,
    borderWidth: hairline,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginBottom: 14,
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 4,
    gap: 8,
  },
  metaValue: {
    fontWeight: fontWeight.semibold,
    flexShrink: 1,
    textAlign: 'right',
  },
  metaDivider: {
    height: hairline,
    marginVertical: 2,
  },
  section: {
    marginBottom: 12,
  },
  sectionTitle: {
    fontWeight: fontWeight.semibold,
    marginBottom: 6,
  },
  historyList: {
    gap: 10,
  },
  versionItem: {
    gap: 3,
  },
  versionTag: {
    fontWeight: fontWeight.semibold,
  },
  desc: {
    lineHeight: 20,
  },
  tip: {
    paddingHorizontal: 18,
    paddingBottom: 12,
    textAlign: 'center',
    fontWeight: fontWeight.medium,
  },
  btns: {
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 18,
    paddingBottom: 18,
    paddingTop: 2,
  },
  btn: {
    flex: 1,
    height: 44,
    borderRadius: radius.pill,
    borderWidth: hairline,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 10,
  },
  primaryBtn: {
    shadowOpacity: 0.3,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
  btnDisabled: {
    opacity: 0.45,
  },
  btnText: {
    fontWeight: fontWeight.semibold,
  },
})

export default VersionModal
