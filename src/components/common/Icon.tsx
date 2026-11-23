import { createIconSetFromIcoMoon } from 'react-native-vector-icons'
import IconIonicons from 'react-native-vector-icons/Ionicons'
import icoMoonConfig from '@/resources/fonts/selection.json'
import { scaleSizeW } from '@/utils/pixelRatio'
import { memo, type ComponentProps } from 'react'
import { useTextShadow, useTheme } from '@/store/theme/hook'
import { StyleSheet, type StyleProp, type TextStyle } from 'react-native'

const IcoMoon = createIconSetFromIcoMoon(icoMoonConfig)

/**
 * 将原洛雪 icomoon 图标名无缝映射到 iOS 风格的 Ionicons，
 * 彻底淘汰旧粗笨图标与 Android 竖三点，未映射图标回退至 IcoMoon。
 */
const ICOMOON_TO_IONICONS: Record<string, string> = {
  menu: 'ellipsis-horizontal',
  'chevron-left': 'chevron-back',
  'chevron-right': 'chevron-forward',
  'chevron-down': 'chevron-down',
  play: 'play',
  pause: 'pause',
  prev: 'play-back',
  next: 'play-forward',
  'search-2': 'search',
  setting: 'settings-outline',
  home: 'albums-outline',
  leaderboard: 'compass-outline',
  album: 'disc-outline',
  music_time: 'musical-notes',
  love: 'heart-outline',
  add_music: 'add-circle-outline',
  'list-add': 'add',
  comment: 'chatbubble-outline',
  lyric: 'chatbox-ellipses-outline',
  available_updates: 'refresh',
  remove: 'close',
  'single-loop': 'repeat',
  'list-loop': 'repeat',
  'list-random': 'shuffle',
  'list-order': 'reorder-three-outline',
  stop: 'timer-outline',
  slider: 'options-outline',
  'download-2': 'arrow-down-circle-outline',
  share: 'share-outline',
  plus: 'add',
  minus: 'remove',
  check: 'checkmark',
  trash: 'trash-outline',
  edit: 'create-outline',
  info: 'information-circle-outline',
  folder: 'folder-open-outline',
  sync: 'sync-outline',
  help: 'help-circle-outline',
}

type IconType = ReturnType<typeof createIconSetFromIcoMoon>

interface IconProps extends Omit<ComponentProps<IconType>, 'style'> {
  style?: StyleProp<TextStyle>
  rawSize?: number
}

export const Icon = memo(({ name, size = 15, rawSize, color, style, ...props }: IconProps) => {
  const theme = useTheme()
  const textShadow = useTextShadow()
  const newStyle = textShadow ? StyleSheet.compose({
    textShadowColor: theme['c-primary-dark-300-alpha-800'],
    textShadowOffset: { width: 0.2, height: 0.2 },
    textShadowRadius: 2,
  }, style) : style
  const resolvedSize = rawSize ?? scaleSizeW(size)
  const resolvedColor = color ?? theme['c-font']
  const mappedIonName = ICOMOON_TO_IONICONS[name]

  if (mappedIonName) {
    return (
      <IconIonicons
        name={mappedIonName}
        size={resolvedSize}
        color={resolvedColor}
        // @ts-expect-error react-native-vector-icons bundled types mismatch
        style={newStyle}
      />
    )
  }

  return (
    <IcoMoon
      name={name}
      size={resolvedSize}
      color={resolvedColor}
      // @ts-expect-error
      style={newStyle}
      {...props}
    />
  )
})

interface IonIconProps {
  name: string
  size?: number
  rawSize?: number
  color?: string
  style?: StyleProp<TextStyle>
}

/** 直接使用 Ionicons（iOS / SF Symbols 风格） */
export const IonIcon = memo(({ name, size = 20, rawSize, color, style }: IonIconProps) => {
  const theme = useTheme()
  return (
    <IconIonicons
      name={name}
      size={rawSize ?? scaleSizeW(size)}
      color={color ?? theme['c-font']}
      // @ts-expect-error react-native-vector-icons bundled types mismatch
      style={style}
    />
  )
})

export { IconIonicons }
