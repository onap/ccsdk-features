import React from 'react';

type MenuIconPropsBase = {
  className?: string;
  size?: number | string;
};

type MenuIconPropsWithColor = MenuIconPropsBase & {
  color: string;
};

type MenuIconProps = MenuIconPropsBase | MenuIconPropsWithColor;

const MenuIcon = (props: MenuIconProps) => {
  const { className, size = '30px' } = props;
  const color = 'color' in props ? props.color : '#36A9E1';

  return (
    <svg className={className} width={size} height={size} viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink">
      <path fill={color} d="M4,10h24c1.104,0,2-0.896,2-2s-0.896-2-2-2H4C2.896,6,2,6.896,2,8S2.896,10,4,10z" />
      <path fill={color} d="M28,14H4c-1.104,0-2,0.896-2,2  s0.896,2,2,2h24c1.104,0,2-0.896,2-2S29.104,14,28,14z" />
      <path fill={color} d="M28,22H4c-1.104,0-2,0.896-2,2s0.896,2,2,2h24c1.104,0,2-0.896,2-2  S29.104,22,28,22z" />
    </svg>
  );
};

MenuIcon.defaultName = 'MenuIcon';

export default MenuIcon;