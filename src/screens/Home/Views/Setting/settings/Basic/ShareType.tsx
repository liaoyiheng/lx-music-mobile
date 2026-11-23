import { memo, useMemo } from 'react'
import { StyleSheet, View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { useSettingValue } from '@/store/setting/hook'
import { useI18n } from '@/lang'
import { updateSetting } from '@/core/common'
import { spacing } from '@/theme/apple'

type ShareType = LX.AppSetting['common.shareType']

const setShareType = (type: ShareType) => {
  updateSetting({ 'common.shareType': type })
}

const useActive = (type: ShareType) => {
  const shareType = useSettingValue('common.shareType')
  const isActive = useMemo(() => shareType == type, [shareType, type])
  return isActive
}

const Item = ({ id, name, showSeparator }: {
  id: ShareType
  name: string
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  return (
    <RadioRow
      label={name}
      selected={isActive}
      onPress={() => { setShareType(id) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()
  const list = useMemo(() => {
    return [
      {
        id: 'system',
        name: t('setting_basic_share_type_system'),
      },
      {
        id: 'clipboard',
        name: t('setting_basic_share_type_clipboard'),
      },
    ] as const
  }, [t])

  return (
    <SubTitle title={t('setting_basic_share_type')}>
      <View style={styles.list}>
        {
          list.map(({ id, name }, index) => (
            <Item
              name={name}
              id={id}
              key={id}
              showSeparator={index < list.length - 1}
            />
          ))
        }
      </View>
    </SubTitle>
  )
})

const styles = StyleSheet.create({
  list: {
    marginHorizontal: -spacing.lg,
    marginBottom: -spacing.md,
  },
})
