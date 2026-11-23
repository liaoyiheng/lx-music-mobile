import { memo, useMemo } from 'react'
import { View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { useSettingValue } from '@/store/setting/hook'
import { useI18n } from '@/lang'
import { setDesktopLyricTextPosition } from '@/core/desktopLyric'
import { createStyle } from '@/utils/tools'
import { updateSetting } from '@/core/common'
import { spacing } from '@/theme/apple'

type X_TYPE = LX.AppSetting['desktopLyric.textPosition.x']

const X_LIST = [
  'left',
  'center',
  'right',
] as const

const useActive = (id: X_TYPE) => {
  const x = useSettingValue('desktopLyric.textPosition.x')
  const isActive = useMemo(() => x == id, [x, id])
  return isActive
}

const Item = ({ id, name, change, showSeparator }: {
  id: X_TYPE
  name: string
  change: (id: X_TYPE) => void
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  return (
    <RadioRow
      label={name}
      selected={isActive}
      onPress={() => { change(id) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()
  const list = useMemo(() => {
    return X_LIST.map(id => ({ id, name: t(`setting_lyric_desktop_text_x_${id}`) }))
  }, [t])

  const setPosition = (id: X_TYPE) => {
    void setDesktopLyricTextPosition(id, null).then(() => {
      updateSetting({ 'desktopLyric.textPosition.x': id })
    })
  }

  return (
    <SubTitle title={t('setting_lyric_desktop_text_x')}>
      <View style={styles.list}>
        {
          list.map(({ id, name }, index) => (
            <Item
              name={name}
              id={id}
              key={id}
              change={setPosition}
              showSeparator={index < list.length - 1}
            />
          ))
        }
      </View>
    </SubTitle>
  )
})

const styles = createStyle({
  list: {
    marginHorizontal: -spacing.lg,
    marginBottom: -spacing.md,
  },
})
