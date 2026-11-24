import { useImperativeHandle, forwardRef, useMemo, useRef, useState, type Ref } from 'react'
import { View, Animated, TouchableHighlight } from 'react-native'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import { useWindowSize } from '@/utils/hooks'

import Modal, { type ModalType } from './Modal'

import { createStyle } from '@/utils/tools'
import { useApple, fontWeight, hairline } from '@/theme/apple'
import Text from './Text'

const MENU_ITEM_HEIGHT = 44
const MENU_VERTICAL_PADDING = 4
const MENU_MIN_WIDTH = 200
const MENU_MAX_WIDTH = 268
const HORIZONTAL_PADDING = 16
const SCREEN_MARGIN = 12

export interface Position { w: number, h: number, x: number, y: number, menuWidth?: number, menuHeight?: number }
export interface MenuSize { width?: number, height?: number }
export type Menus = Readonly<Array<{ action: string, label: string, disabled?: boolean, destructive?: boolean }>>

const estimateTextWidth = (text: string, fontSize: number) => {
  let width = 0
  for (let i = 0; i < text.length; i++) {
    width += text.charCodeAt(i) > 255 ? fontSize : fontSize * 0.62
  }
  return Math.ceil(width)
}

const styles = createStyle({
  menu: {
    position: 'absolute',
  },
  menuInner: {
    flex: 1,
    paddingVertical: MENU_VERTICAL_PADDING,
  },
  menuItem: {
    paddingHorizontal: HORIZONTAL_PADDING,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  menuItemCenter: {
    justifyContent: 'center',
  },
  label: {
    flex: 1,
  },
  centerCheck: {
    position: 'absolute',
    right: HORIZONTAL_PADDING,
  },
  divider: {
    height: hairline,
    marginHorizontal: 14,
  },
})

interface Props<M extends Menus = Menus> {
  menus: Readonly<M>
  onPress?: (menu: M[number]) => void
  buttonPosition: Position
  menuSize: MenuSize
  onHide: () => void
  width?: number
  height?: number
  fontSize?: number
  center?: boolean
  activeId?: M[number]['action'] | null
}

const Menu = ({
  buttonPosition,
  menuSize,
  menus,
  width,
  height,
  onPress = () => {},
  onHide,
  activeId,
  fontSize = 15,
  center = false,
}: Props) => {
  const apple = useApple()
  const windowSize = useWindowSize()

  const menuItemStyle = useMemo(() => {
    const longestLabelWidth = menus.reduce((max, item) => {
      return Math.max(max, estimateTextWidth(item.label, fontSize) + HORIZONTAL_PADDING * 2 + 28)
    }, 0)
    const maxAllowed = Math.min(MENU_MAX_WIDTH, windowSize.width - SCREEN_MARGIN * 2)
    const minAllowed = Math.min(MENU_MIN_WIDTH, maxAllowed)
    const preferredWidth = Math.max(width ?? 0, menuSize.width ?? 0, longestLabelWidth)
    return {
      width: Math.max(minAllowed, Math.min(maxAllowed, preferredWidth)),
      height: height ?? MENU_ITEM_HEIGHT,
    }
  }, [menus, fontSize, width, menuSize.width, height, windowSize.width])

  const menuStyle = useMemo(() => {
    let menuHeight = menus.length * menuItemStyle.height + MENU_VERTICAL_PADDING * 2
    const verticalMargin = 20
    const topHeight = buttonPosition.y - verticalMargin
    const bottomHeight = windowSize.height - buttonPosition.y - buttonPosition.h - verticalMargin
    if (menuHeight > topHeight && menuHeight > bottomHeight) {
      menuHeight = Math.max(topHeight, bottomHeight, menuItemStyle.height + MENU_VERTICAL_PADDING * 2)
    }

    const menuWidth = menuItemStyle.width
    const showInBottom = bottomHeight >= menuHeight || bottomHeight >= topHeight
    let top = showInBottom
      ? buttonPosition.y + buttonPosition.h + 6
      : buttonPosition.y - menuHeight - 6
    top = Math.max(verticalMargin, Math.min(top, windowSize.height - menuHeight - verticalMargin))

    let left = buttonPosition.x
    if (left + menuWidth > windowSize.width - SCREEN_MARGIN) {
      left = buttonPosition.x + buttonPosition.w - menuWidth
    }
    left = Math.max(SCREEN_MARGIN, Math.min(left, windowSize.width - menuWidth - SCREEN_MARGIN))

    return {
      height: menuHeight,
      width: menuWidth,
      top,
      left,
    }
  }, [menus.length, menuItemStyle, buttonPosition, windowSize])

  const menuPress = (menu: Menus[number]) => {
    onPress(menu)
    onHide()
  }

  return (
    <View
      style={[styles.menu, menuStyle]}
      onStartShouldSetResponder={() => true}
    >
      <GlassSurface
        dense
        borderRadius={16}
        style={{ width: menuStyle.width, height: menuStyle.height }}
        innerStyle={styles.menuInner}
      >
        <Animated.ScrollView keyboardShouldPersistTaps={'always'}>
          {
            menus.map((menu, index) => {
              const textColor = menu.destructive ? apple.destructive : apple.label
              const divider = index > 0
                ? <View style={[styles.divider, { backgroundColor: apple.separator }]} />
                : null

              if (menu.disabled) {
                return (
                  <View key={menu.action}>
                    {divider}
                    <View
                      style={[
                        styles.menuItem,
                        center && styles.menuItemCenter,
                        {
                          width: menuItemStyle.width,
                          height: menuItemStyle.height,
                          opacity: 0.4,
                        },
                      ]}
                    >
                      <Text
                        style={[styles.label, { textAlign: center ? 'center' : 'left' }]}
                        color={textColor}
                        size={fontSize}
                        numberOfLines={1}
                      >
                        {menu.label}
                      </Text>
                    </View>
                  </View>
                )
              }

              if (menu.action == activeId) {
                return (
                  <View key={menu.action}>
                    {divider}
                    <View
                      style={[
                        styles.menuItem,
                        center && styles.menuItemCenter,
                        {
                          width: menuItemStyle.width,
                          height: menuItemStyle.height,
                        },
                      ]}
                    >
                      <Text
                        style={[
                          styles.label,
                          {
                            textAlign: center ? 'center' : 'left',
                            fontWeight: fontWeight.semibold,
                          },
                        ]}
                        color={apple.accent}
                        size={fontSize}
                        numberOfLines={1}
                      >
                        {menu.label}
                      </Text>
                      <IonIcon
                        name="checkmark"
                        rawSize={17}
                        color={apple.accent}
                        style={center ? styles.centerCheck : undefined}
                      />
                    </View>
                  </View>
                )
              }

              return (
                <View key={menu.action}>
                  {divider}
                  <TouchableHighlight
                    style={{
                      width: menuItemStyle.width,
                      height: menuItemStyle.height,
                    }}
                    underlayColor={apple.highlight}
                    activeOpacity={0.78}
                    onPress={() => { menuPress(menu) }}
                  >
                    <View
                      style={[
                        styles.menuItem,
                        center && styles.menuItemCenter,
                        {
                          width: menuItemStyle.width,
                          height: menuItemStyle.height,
                        },
                      ]}
                    >
                      <Text
                        style={[
                          styles.label,
                          {
                            textAlign: center ? 'center' : 'left',
                            fontWeight: menu.destructive ? fontWeight.semibold : fontWeight.regular,
                          },
                        ]}
                        color={textColor}
                        size={fontSize}
                        numberOfLines={1}
                      >
                        {menu.label}
                      </Text>
                    </View>
                  </TouchableHighlight>
                </View>
              )
            })
          }
        </Animated.ScrollView>
      </GlassSurface>
    </View>
  )
}

export interface MenuProps<M extends Menus = Menus> {
  menus: M
  onPress: (menu: M[number]) => void
  onHide?: () => void
  width?: number
  height?: number
  fontSize?: number
  center?: boolean
  activeId?: M[number]['action'] | null
}

export interface MenuType {
  show: (position: Position, menuSize?: MenuSize) => void
  hide: () => void
}

const Component = <M extends Menus>({ menus, width, height, activeId, onHide, onPress, fontSize, center }: MenuProps<M>, ref: Ref<MenuType>) => {
  const modalRef = useRef<ModalType>(null)
  const [position, setPosition] = useState<Position>({ w: 0, h: 0, x: 0, y: 0 })
  const [menuSize, setMenuSize] = useState<MenuSize>({})
  const hide = () => {
    modalRef.current?.setVisible(false)
  }
  useImperativeHandle(ref, () => ({
    show(newPosition, menuSize) {
      setPosition(newPosition)
      if (menuSize) setMenuSize(menuSize)
      modalRef.current?.setVisible(true)
    },
    hide() {
      hide()
    },
  }))

  return (
    <Modal onHide={onHide} ref={modalRef}>
      <Menu menus={menus} width={width} height={height} activeId={activeId} buttonPosition={position} menuSize={menuSize} onPress={onPress} onHide={hide} fontSize={fontSize} center={center} />
    </Modal>
  )
}

export default forwardRef(Component) as <M extends Menus>(p: MenuProps<M> & { ref?: Ref<MenuType> }) => JSX.Element | null
