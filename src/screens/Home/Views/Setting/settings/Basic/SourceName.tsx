import { memo, useMemo } from 'react'
import { StyleSheet, View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { useSettingValue } from '@/store/setting/hook'
import { useI18n } from '@/lang'
import { updateSetting } from '@/core/common'
import { spacing } from '@/theme/apple'

type SourceNameType = LX.AppSetting['common.sourceNameType']

const setSourceNameType = (type: SourceNameType) => {
  updateSetting({ 'common.sourceNameType': type })
}

const useActive = (type: SourceNameType) => {
  const sourceNameType = useSettingValue('common.sourceNameType')
  const isActive = useMemo(() => sourceNameType == type, [sourceNameType, type])
  return isActive
}

const Item = ({ id, name, showSeparator }: {
  id: SourceNameType
  name: string
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  return (
    <RadioRow
      label={name}
      selected={isActive}
      onPress={() => { setSourceNameType(id) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()
  const list = useMemo(() => {
    return [
      {
        id: 'real',
        name: t('setting_basic_sourcename_real'),
      },
      {
        id: 'alias',
        name: t('setting_basic_sourcename_alias'),
      },
    ] as const
  }, [t])

  return (
    <SubTitle title={t('setting_basic_sourcename')} showSeparator={false}>
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
