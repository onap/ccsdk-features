/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */

import * as React from 'react';

import type { FC } from 'react';
import * as d3 from 'd3';

import { useD3 } from "../hooks/d3";
import { GPSProfileResult } from "../model/GPSProfileResult";
import { max } from '../utils/math';

type HeightMapProps = {
  data: GPSProfileResult[];
  dataMin: GPSProfileResult;
  dataMax: GPSProfileResult;
  width: number;
  height: number;
  heightPosA: number;
  heightPosB: number;
}

const HeightChart: FC<HeightMapProps> = (props) => {
  const { data, dataMin, dataMax, heightPosA, heightPosB } = props;
  let ref: React.RefObject<SVGSVGElement>

  const drawSvg = () => {
    ref = useD3(
      (svg) => {
        const margin = 100;
        const width = Number(svg.attr("width")) - margin;
        const height = Number(svg.attr("height")) - margin;

        // Add X axis
        const x = d3.scaleBand()
          .range([0, width])
          .domain(data.map(d => (`${d.gps.latitude},${d.gps.latitude}`)))
          .padding(0.2);

        const maxHeight = max([dataMax.height, heightPosA, heightPosB], d => d)

        // Add Y axis
        const y = d3.scaleLinear()
          .domain([dataMin.height, maxHeight])
          .range([height, 0]);

        svg.append("g")
          .attr('transform', `translate(${margin / 2}, ${margin / 2})`)
          .call(d3.axisLeft(y));

        // Bars
        svg.selectAll("myBar")
          .data(data)
          .join("rect")
          .attr('transform', `translate(${margin / 2}, ${margin / 2})`)
          .attr("x", d => x(`${d.gps.latitude},${d.gps.latitude}`) || '')
          .attr("y", d => y(d.height))
          .attr("width", x.bandwidth())
          .attr("fill", "#69b3a2b0")
          .attr("height", d => height - y(d.height)) // always equal to 0

        const firstX = `${data[0].gps.latitude},${data[0].gps.latitude}`
        const lastX = `${data[data.length - 1].gps.latitude},${data[data.length - 1].gps.latitude}`;

        //add line
        const x1 = x(firstX)!;
        const x2 = x(lastX)!;

        svg.append("line")
          .attr('transform', `translate(${margin / 2}, ${margin / 2})`)
          .attr("x1", x1)
          .attr("y1", y(props.heightPosA))
          .attr("x2", x2)
          .attr("y2", y(props.heightPosB)) 

          .style("stroke", "#88A")
          .attr("stroke-width", "3px")

        //append circle on start and end 

        svg.append("circle")
          .attr('transform', `translate(${margin / 2}, ${margin / 2})`)
          .attr('cx', x1)
          .attr('cy', y(props.heightPosA))
          .attr('r', 10)
          .attr('stroke', '#223b53')
          .attr('fill', '#225ba3');

        svg.append("circle")
          .attr('transform', `translate(${margin / 2}, ${margin / 2})`)
          .attr('cx', x2)
          .attr('cy', y(props.heightPosB))
          .attr('r', 10)
          .attr('stroke', '#223b53')
          .attr('fill', '#225ba3');
      },
      [data]
    );
  }

  drawSvg();



  return (
    <svg ref={ref!} width={props.width} height={props.height} />

  );
}

export { HeightChart };
