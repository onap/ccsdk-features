.. contents::
   :depth: 3
..

Network Map
===========

The ‘Network Map’ visualizes a network by showing the location of a site
and its connections (links) to other sites in a geographical context.

Views
-----

The ‘Network Map’ consists of two side-by-side views: The map and the
details-panel.

Map
~~~

The geographical map visualizes sites and links of a network. Sites are
usually displayed as blue circles and links are shown as lines
connecting sites.

If a link or site is clicked, its information is presented in the
details panel. If more than one site or link is clicked, or if links or
sites are too close together to determine which element should be
selected, a selection popup appears to select one of the elements.

The map offers statistics information to visualize the number of links
and sites in the currently shown map area. The statistics information
gets updated when the map stops moving.

Additionally, the map offers a search field. The user can enter the name
of a site or link. If an element was found, the map will center on the
given element and its information is loaded by the details panel.

If the zoom level is bigger than 11 and the loaded sites have a type of
high-rise building, datacenter, factory, or street-lamp, the blue
circles are swapped against icons, which visualizes the type of site.

The swapping of icons can be activated or deactivated via a switch on
the left-hand site of the map. The switch only becomes visible, if the
zoom level is bigger than 9.

The map supports zoom levels between 0 (furthest zoomed out, the entire
world is visible) and 18 (most detailed).

Whenever the map stops moving, it updates the URL with its current
latitude, longitude, and zoom values. If the ‘Network Map’ application
is opened with those URL parameters present, it will display the given
area. That way, the map can be bookmarked or shared and will always
display the same result.

Details
~~~~~~~

The details panel shows information specific to the selected element.

Sites offer information about itself, such as name, address and owner,
and a short overview of its links and nodes data. The nodes are physical
network elements, comparable to the elements of the ‘connect’
application, and offer an interface to other apps via buttons, such as
connect, configure, and fault. Currently, those buttons are disabled. By
clicking on a link, the given link is loaded into details.

If a link of type ‘microwave’ is selected, the ‘calculate link’ button
is available, which opens the :doc:`Link Calculator <linkCalculator>`
in a new tab or page.

Just like the map, the details panel updates the URL if data is loaded.
Once again, the ‘Network Map’ application will try to load the element
specified in the URL, if one is present.

Connection Error
----------------

If no tile or network data is available, an error popup is shown.

Load Network- and Tile-Data
---------------------------

On startup of the sdnc-web container, a topology URL for the network
data and a tile URL for the tiles can be specified.

A ready-to-use topology server offering pre-defined network data is
available here. There is no way to import generic network data as of
now.
