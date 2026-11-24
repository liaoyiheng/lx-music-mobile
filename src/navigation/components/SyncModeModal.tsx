import { useEffect, useState } from 'react'
import { View, ScrollView } from 'react-native'

import Button from '@/components/common/Button'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import ModalContent from './ModalContent'
import syncState from '@/store/sync/state'
import CheckBox from '@/components/common/CheckBox'
import { setSyncModeComponentId } from '@/core/sync'
import { useApple, fontWeight, hairline, radius } from '@/theme/apple'

const styles = createStyle({
  main: {
    flexShrink: 1,
    paddingTop: 20,
    paddingHorizontal: 18,
    paddingBottom: 12,
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
  btnGroup: {
    marginTop: 12,
  },
  groupLabel: {
    fontWeight: fontWeight.semibold,
    marginBottom: 6,
  },
  btns: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  btn: {
    flexGrow: 1,
    minWidth: 112,
    height: 40,
    paddingHorizontal: 12,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: radius.pill,
    borderWidth: hairline,
  },
  primaryBtn: {
    shadowOpacity: 0.28,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 3 },
    elevation: 3,
  },
  btnText: {
    fontWeight: fontWeight.semibold,
  },
  checkboxWrap: {
    marginTop: 8,
  },
  tips: {
    paddingHorizontal: 18,
    paddingBottom: 18,
    paddingTop: 4,
    gap: 4,
  },
  tipTitle: {
    fontWeight: fontWeight.semibold,
  },
  tip: {
    lineHeight: 17,
  },
})

const ListModeModal = () => {
  const apple = useApple()
  const t = useI18n()
  const [isOverwrite, setOverwrite] = useState(false)

  const handleSelectMode = (mode: LX.Sync.List.SyncMode) => {
    if (mode.startsWith('overwrite') && isOverwrite) mode += '_full'
    global.app_event.selectSyncMode({ type: 'list', mode })
  }

  return (
    <>
      <View style={styles.main}>
        <Text style={styles.title} size={16} color={apple.label}>{t('sync__list_mode_title', { name: syncState.serverName })}</Text>
        <ScrollView style={styles.content} keyboardShouldPersistTaps={'always'}>
          <View style={[styles.btnGroup, { marginTop: 0 }]}>
            <Text size={13} color={apple.secondaryLabel} style={styles.groupLabel}>{t('sync__mode_merge_tip')}</Text>
            <View style={styles.btns}>
              <Button
                activeOpacity={0.75}
                style={[styles.btn, styles.primaryBtn, { backgroundColor: apple.accent, borderColor: 'rgba(255, 255, 255, 0.34)', shadowColor: apple.accent }]}
                onPress={() => { handleSelectMode('merge_local_remote') }}
              >
                <Text size={13} color={apple.onAccent} style={styles.btnText}>{t('sync__mode_merge_btn_local_remote')}</Text>
              </Button>
              <Button
                activeOpacity={0.75}
                style={[styles.btn, styles.primaryBtn, { backgroundColor: apple.accent, borderColor: 'rgba(255, 255, 255, 0.34)', shadowColor: apple.accent }]}
                onPress={() => { handleSelectMode('merge_remote_local') }}
              >
                <Text size={13} color={apple.onAccent} style={styles.btnText}>{t('sync__mode_merge_btn_remote_local')}</Text>
              </Button>
            </View>
          </View>
          <View style={styles.btnGroup}>
            <Text size={13} color={apple.secondaryLabel} style={styles.groupLabel}>{t('sync__mode_overwrite_label')}</Text>
            <View style={styles.btns}>
              <Button
                activeOpacity={0.65}
                style={[styles.btn, { backgroundColor: apple.fill, borderColor: apple.glassBorder }]}
                onPress={() => { handleSelectMode('overwrite_local_remote') }}
              >
                <Text size={13} color={apple.label} style={styles.btnText}>{t('sync__mode_overwrite_btn_local_remote')}</Text>
              </Button>
              <Button
                activeOpacity={0.65}
                style={[styles.btn, { backgroundColor: apple.fill, borderColor: apple.glassBorder }]}
                onPress={() => { handleSelectMode('overwrite_remote_local') }}
              >
                <Text size={13} color={apple.label} style={styles.btnText}>{t('sync__mode_overwrite_btn_remote_local')}</Text>
              </Button>
            </View>
            <View style={styles.checkboxWrap}>
              <CheckBox check={isOverwrite} onChange={setOverwrite} label={t('sync__mode_overwrite')} />
            </View>
          </View>
          <View style={styles.btnGroup}>
            <Text size={13} color={apple.secondaryLabel} style={styles.groupLabel}>{t('sync__mode_other_label')}</Text>
            <View style={styles.btns}>
              <Button
                activeOpacity={0.65}
                style={[styles.btn, { backgroundColor: apple.fill, borderColor: apple.glassBorder }]}
                onPress={() => { handleSelectMode('cancel') }}
              >
                <Text size={13} color={apple.secondaryLabel} style={styles.btnText}>{t('sync__mode_overwrite_btn_cancel')}</Text>
              </Button>
            </View>
          </View>
        </ScrollView>
      </View>
      <View style={styles.tips}>
        <Text style={styles.tip} size={12} color={apple.secondaryLabel}>
          <Text style={styles.tipTitle} size={12} color={apple.label}>{t('sync__mode_merge_tip')}</Text>
          {t('sync__list_mode_merge_tip_desc')}
        </Text>
        <Text style={styles.tip} size={12} color={apple.secondaryLabel}>
          <Text style={styles.tipTitle} size={12} color={apple.label}>{t('sync__mode_overwrite_tip')}</Text>
          {t('sync__list_mode_overwrite_tip_desc')}
        </Text>
        <Text style={styles.tip} size={12} color={apple.secondaryLabel}>
          <Text style={styles.tipTitle} size={12} color={apple.label}>{t('sync__mode_other_tip')}</Text>
          {t('sync__list_mode_other_tip_desc')}
        </Text>
      </View>
    </>
  )
}

const DislikeModeModal = () => {
  const apple = useApple()
  const t = useI18n()
  const handleSelectMode = (mode: LX.Sync.Dislike.SyncMode) => {
    global.app_event.selectSyncMode({ type: 'dislike', mode })
  }

  return (
    <>
      <View style={styles.main}>
        <Text style={styles.title} size={16} color={apple.label}>{t('sync__dislike_mode_title', { name: syncState.serverName })}</Text>
        <ScrollView style={styles.content} keyboardShouldPersistTaps={'always'}>
          <View style={[styles.btnGroup, { marginTop: 0 }]}>
            <Text size={13} color={apple.secondaryLabel} style={styles.groupLabel}>{t('sync__mode_merge_tip')}</Text>
            <View style={styles.btns}>
              <Button
                activeOpacity={0.75}
                style={[styles.btn, styles.primaryBtn, { backgroundColor: apple.accent, borderColor: 'rgba(255, 255, 255, 0.34)', shadowColor: apple.accent }]}
                onPress={() => { handleSelectMode('merge_local_remote') }}
              >
                <Text size={13} color={apple.onAccent} style={styles.btnText}>{t('sync__mode_merge_btn_local_remote')}</Text>
              </Button>
              <Button
                activeOpacity={0.75}
                style={[styles.btn, styles.primaryBtn, { backgroundColor: apple.accent, borderColor: 'rgba(255, 255, 255, 0.34)', shadowColor: apple.accent }]}
                onPress={() => { handleSelectMode('merge_remote_local') }}
              >
                <Text size={13} color={apple.onAccent} style={styles.btnText}>{t('sync__mode_merge_btn_remote_local')}</Text>
              </Button>
            </View>
          </View>
          <View style={styles.btnGroup}>
            <Text size={13} color={apple.secondaryLabel} style={styles.groupLabel}>{t('sync__mode_overwrite_label')}</Text>
            <View style={styles.btns}>
              <Button
                activeOpacity={0.65}
                style={[styles.btn, { backgroundColor: apple.fill, borderColor: apple.glassBorder }]}
                onPress={() => { handleSelectMode('overwrite_local_remote') }}
              >
                <Text size={13} color={apple.label} style={styles.btnText}>{t('sync__mode_overwrite_btn_local_remote')}</Text>
              </Button>
              <Button
                activeOpacity={0.65}
                style={[styles.btn, { backgroundColor: apple.fill, borderColor: apple.glassBorder }]}
                onPress={() => { handleSelectMode('overwrite_remote_local') }}
              >
                <Text size={13} color={apple.label} style={styles.btnText}>{t('sync__mode_overwrite_btn_remote_local')}</Text>
              </Button>
            </View>
          </View>
          <View style={styles.btnGroup}>
            <Text size={13} color={apple.secondaryLabel} style={styles.groupLabel}>{t('sync__mode_other_label')}</Text>
            <View style={styles.btns}>
              <Button
                activeOpacity={0.65}
                style={[styles.btn, { backgroundColor: apple.fill, borderColor: apple.glassBorder }]}
                onPress={() => { handleSelectMode('cancel') }}
              >
                <Text size={13} color={apple.secondaryLabel} style={styles.btnText}>{t('sync__mode_overwrite_btn_cancel')}</Text>
              </Button>
            </View>
          </View>
        </ScrollView>
      </View>
      <View style={styles.tips}>
        <Text style={styles.tip} size={12} color={apple.secondaryLabel}>
          <Text style={styles.tipTitle} size={12} color={apple.label}>{t('sync__mode_merge_tip')}</Text>
          {t('sync__dislike_mode_merge_tip_desc')}
        </Text>
        <Text style={styles.tip} size={12} color={apple.secondaryLabel}>
          <Text style={styles.tipTitle} size={12} color={apple.label}>{t('sync__mode_overwrite_tip')}</Text>
          {t('sync__dislike_mode_overwrite_tip_desc')}
        </Text>
        <Text style={styles.tip} size={12} color={apple.secondaryLabel}>
          <Text style={styles.tipTitle} size={12} color={apple.label}>{t('sync__mode_other_tip')}</Text>
          {t('sync__dislike_mode_other_tip_desc')}
        </Text>
      </View>
    </>
  )
}

export default ({ componentId }: { componentId: string }) => {
  useEffect(() => {
    setSyncModeComponentId(componentId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <ModalContent>
      {
        syncState.type == 'list' ? <ListModeModal />
          : syncState.type == 'dislike' ? <DislikeModeModal />
            : null
      }
    </ModalContent>
  )
}
