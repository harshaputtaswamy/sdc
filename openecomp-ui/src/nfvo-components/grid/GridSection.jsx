/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

const GridSection = ({
    title,
    children,
    className = '',
    titleClassName,
    hasLastColSet = false,
    required = false
}) => {
    return (
        <div
            className={classnames('grid-section', className, {
                'has-last-col-set': hasLastColSet
            })}>
            {title && (
                <div className={`section-title ${titleClassName || ''}`}>
                    {title}
                    {required && <span className={'required'}>*</span>}
                </div>
            )}
            <div className="grid-items">{children}</div>
        </div>
    );
};

GridSection.propTypes = {
    title: PropTypes.string,
    titleClassName: PropTypes.string,
    hasLastColSet: PropTypes.bool
};

export default GridSection;
