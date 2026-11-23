import { memo, useMemo } from 'react'
import { StyleSheet, View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { useSettingValue } from '@/store/setting/hook'
import { useI18n } from '@/lang'
import { updateSetting } from '@/core/common'
import { spacing } from '@/theme/apple'

const setAddMusicLocationType = (type: LX.AddMusicLocationType) => {
  updateSetting({ 'list.addMusicLocationType': type })
}

const useActive = (id: LX.AddMusicLocationType) => {
  const addMusicLocationType = useSettingValue('list.addMusicLocationType')
  const isActive = useMemo(() => addMusicLocationType == id, [addMusicLocationType, id])
  return isActive
}

const Item = ({ id, name, showSeparator }: {
  id: LX.AddMusicLocationType
  name: string
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  return (
    <RadioRow
      label={name}
      selected={isActive}
      onPress={() => { setAddMusicLocationType(id) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()

  return (
    <SubTitle title={t('setting_list_add_music_location_type')} showSeparator={false}>
      <View style={styles.list}>
        <Item id="top" name={t('setting_list_add_music_location_type_top')} showSeparator={true} />
        <Item id="bottom" name={t('setting_list_add_music_location_type_bottom')} showSeparator={false} />
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
