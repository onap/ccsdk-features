.. contents::
   :depth: 3
..

Link Calculator
===============

The ‘Link calculator’ analyzes the microwave propagation measurements of
the wireless links. It can be accessed through the Network Map by
clicking on the ‘link calculation’ button available in microwave links.

View
----

The app includes two view possibilities. If it is accessed via the menu,
the view provides a form table and a blank information table. The form
table offers inputs for latitude and longitude values of the two points
of a link. By entering this geographical information the data in the
information table gets updated.

The information table contains the calculation inputs and outputs. If
the Link Calculator is accessed through the ‘calculate link’ button,
only this table with pre-filled geographical locations is shown.
Currently, input variables of the link calculation include Polarization,
Frequency, Rain Model, and Rainfall Rate. Outputs of the calculation are
Free Space Loss and Rain Loss. The results will be visible upon clicking
the ‘Calculate’ button at the bottom of the table.

Average Mean Sea Level
~~~~~~~~~~~~~~~~~~~~~~

Denotes the ground elevation of the sites on each end.

Antenna Height Above Ground
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Is the height at which the antenna is mounted from the ground.

Distance
~~~~~~~~

The distance in the information table is auto-filled when the microwave
link is selected and the calculator is accessed through the ‘calculate
link’ button in the Network Map. If the points are entered manually, the
distance is calculated after clicking the ‘Calculate’ button.

Polarization
~~~~~~~~~~~~

A selection of Vertical and Horizontal polarization is possible.

Frequency
~~~~~~~~~

A selection of known and regulated microwave bands is possible.

Rainfall Rate
~~~~~~~~~~~~~

Rainfall rate can be entered in the field, however if the local
information is not available, the digital map and rainfall values of
ITU-R P.837-7  [1]_ is used. The latitude grid is from -90 North degrees
to +90 North degrees and the longitude grid is from -180 degrees East to
+180 East. For this calculation, the pre-computed R_0.01 map is used. A
selection is possible through the Rain Model drop-down list. When the
ITU model is selected, the rainfall rate will be shown in the rainfall
rate field after clicking the ‘Calculate’ button.

Calculations
------------

Wireless signal attenuation is calculated based on ITU Recommendations
for Propagation. At the moment these calculations include the free space
loss and rain loss.

Free Space Loss
~~~~~~~~~~~~~~~

Calculates the Free Space Path Loss for a point-to-point non-terrestrial
link using the recommended formula in ITU-R P.525-4  [2]_. The output is
shown in dB hence the distance is attributed in the calculation.

Rain Loss
~~~~~~~~~

Calculates the rain induced attenuation on microwave signal. The
calculation is based on the recommended formula in ITU-R P.838-3  [3]_,
taking into account the polarization of the signal, rainfall rate, and
distance. The manual calculation is also possible if ‘Specific Rain’ is
selected as rain model. After selecting the inputs, rain loss will be
calculated by clicking the ‘Calculate’ button.

--------------

.. [1]
   Radiocommunication Sector of International Telecommunication Union.
   ITU-R P.837-7: Characteristics of precipitation for propagation
   modelling 2017.

.. [2]
   Radiocommunication Sector of International Telecommunication Union.
   ITU-R P.525-4: Calculation of free-space attenuation 2019.

.. [3]
   Radiocommunication Sector of International Telecommunication Union.
   ITU-R P.838-3: Specific attenuation model for rain for use in
   prediction methods 2005.
