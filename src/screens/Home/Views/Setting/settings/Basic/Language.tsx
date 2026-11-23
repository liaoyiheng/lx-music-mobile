import { memo, useMemo } from 'react'
import { StyleSheet, View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import type { I18n } from '@/lang'
import { useI18n, langList } from '@/lang'
import { setLanguage } from '@/core/common'
import { useSettingValue } from '@/store/setting/hook'
import { spacing } from '@/theme/apple'

const useActive = (id: I18n['locale']) => {
  const activeLangId = useSettingValue('common.langId')
  const isActive = useMemo(() => activeLangId == id, [activeLangId, id])
  return isActive
}

const Item = ({ id, name, showSeparator }: {
  id: I18n['locale']
  name: string
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  return (
    <RadioRow
      label={name}
      selected={isActive}
      onPress={() => { setLanguage(id) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()

  return (
    <SubTitle title={t('setting_basic_lang')}>
      <View style={styles.list}>
        {
          langList.map(({ locale, name }, index) => (
            <Item
              name={name}
              id={locale}
              key={locale}
              showSeparator={index < langList.length - 1}
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
