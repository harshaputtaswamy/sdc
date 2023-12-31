/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */


import { Pipe, PipeTransform } from '@angular/core';
import * as _ from 'lodash';

@Pipe({name: 'resourceName'})
export class ResourceNamePipe implements PipeTransform {
    
    public static getDisplayName (value:string): string {
        let nameFirstCapital: string = value.charAt(0).toUpperCase() + value.slice(1);
        const newName:string =
            _.last(nameFirstCapital.split(/tosca\.nodes\..*network\..*relationships\..*org\.openecomp\..*resource\.nfv\..*nodes\.module\..*cp\..*vl\./));
        return (newName) ? newName : value;
    }

    transform(value) : any {
        if (value) {
           return ResourceNamePipe.getDisplayName(value);
        }
    }
}
