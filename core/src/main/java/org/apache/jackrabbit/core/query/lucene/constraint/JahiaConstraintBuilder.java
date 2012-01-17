/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

//package org.apache.jackrabbit.core.query.lucene.constraint;
//
//import org.apache.jackrabbit.commons.query.qom.Operator;
//import org.apache.jackrabbit.core.query.lucene.LuceneQueryFactory;
//import org.apache.jackrabbit.spi.Name;
//import org.apache.jackrabbit.spi.commons.query.qom.*;
//
//import javax.jcr.*;
//import javax.jcr.query.InvalidQueryException;
//import java.net.URLDecoder;
//import java.util.Map;
//
///**
// * 
// * User: toto
// * Date: Aug 2, 2010
// * Time: 6:55:03 PM
// *
// */
//public class JahiaConstraintBuilder extends ConstraintBuilder {
//
//    /**
//     * Creates a {@link org.apache.jackrabbit.core.query.lucene.constraint.Constraint} from a QOM <code>constraint</code>.
//     *
//     * @param constraint         the QOM constraint.
//     * @param bindVariableValues the map of bind variables and their respective
//     *                           value.
//     * @param selectors          the selectors of the current query.
//     * @param factory            the lucene query factory.
//     * @param vf                 the value factory of the current session.
//     * @return a {@link org.apache.jackrabbit.core.query.lucene.constraint.Constraint}.
//     * @throws javax.jcr.RepositoryException if an error occurs while building the
//     *                             constraint.
//     */
//    public static Constraint create(ConstraintImpl constraint,
//                                    Map<String, Value> bindVariableValues,
//                                    SelectorImpl[] selectors,
//                                    LuceneQueryFactory factory,
//                                    ValueFactory vf)
//            throws RepositoryException {
//        try {
//            return (Constraint) constraint.accept(new Visitor(
//                    bindVariableValues, selectors, factory, vf), null);
//        } catch (RepositoryException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RepositoryException(e);
//        }
//    }
//
//    /**
//     * A QOM tree visitor that translates the contraints.
//     */
//    private static final class Visitor extends DefaultQOMTreeVisitor {
//
//        /**
//         * The bind variables and their values.
//         */
//        private final Map<String, Value> bindVariableValues;
//
//        /**
//         * The selectors of the query.
//         */
//        private final SelectorImpl[] selectors;
//
//        /**
//         * The lucene query builder.
//         */
//        private final LuceneQueryFactory factory;
//
//        /**
//         * The value factory of the current session.
//         */
//        private final ValueFactory vf;
//
//        /**
//         * Creates a new visitor.
//         *
//         * @param bindVariableValues the bound values.
//         * @param selectors          the selectors of the current query.
//         * @param factory            the lucene query factory.
//         * @param vf                 the value factory of the current session.
//         */
//        Visitor(Map<String, Value> bindVariableValues,
//                SelectorImpl[] selectors,
//                LuceneQueryFactory factory,
//                ValueFactory vf) {
//            this.bindVariableValues = bindVariableValues;
//            this.selectors = selectors;
//            this.factory = factory;
//            this.vf = vf;
//        }
//
//        public Object visit(AndImpl node, Object data) throws Exception {
//            ConstraintImpl left = (ConstraintImpl) node.getConstraint1();
//            ConstraintImpl right = (ConstraintImpl) node.getConstraint2();
//            return new AndConstraint((Constraint) left.accept(this, null),
//                    (Constraint) right.accept(this, null));
//        }
//
//        public Object visit(BindVariableValueImpl node, Object data)
//                throws Exception {
//            String name = node.getBindVariableName();
//            Value value = bindVariableValues.get(name);
//            if (value != null) {
//                return value;
//            } else {
//                throw new RepositoryException(
//                        "No value specified for bind variable " + name);
//            }
//        }
//
//        public Object visit(ChildNodeImpl node, Object data) throws Exception {
//            return new JahiaChildNodeConstraint(node,
//                    getSelector(node.getSelectorQName()));
//        }
//
//        public Object visit(ComparisonImpl node, Object data) throws Exception {
//            DynamicOperandImpl op1 = (DynamicOperandImpl) node.getOperand1();
//            Operator operator = node.getOperatorInstance();
//            StaticOperandImpl op2 = ((StaticOperandImpl) node.getOperand2());
//            Value staticValue = (Value) op2.accept(this, null);
//
//            DynamicOperand dynOp = (DynamicOperand) op1.accept(this, staticValue);
//            SelectorImpl selector = getSelector(op1.getSelectorQName());
//            if (operator == Operator.LIKE) {
//                return new LikeConstraint(dynOp, staticValue, selector);
//            } else {
//                return new ComparisonConstraint(
//                        dynOp, operator, staticValue, selector);
//            }
//        }
//
//        public Object visit(DescendantNodeImpl node, Object data)
//                throws Exception {
//            return new JahiaDescendantNodeConstraint(node,
//                    getSelector(node.getSelectorQName()));
//        }
//
//        public Object visit(FullTextSearchImpl node, Object data)
//                throws Exception {
//            return new FullTextConstraint(node,
//                    getSelector(node.getSelectorQName()), factory);
//        }
//
//        public Object visit(FullTextSearchScoreImpl node, Object data)
//                throws Exception {
//            return new FullTextSearchScoreOperand();
//        }
//
//        public Object visit(LengthImpl node, Object data) throws Exception {
//            Value staticValue = (Value) data;
//            // make sure it can be converted to Long
//            try {
//                staticValue.getLong();
//            } catch (ValueFormatException e) {
//                throw new InvalidQueryException("Static value " +
//                        staticValue.getString() + " cannot be converted to a Long");
//            }
//            PropertyValueImpl propValue = (PropertyValueImpl) node.getPropertyValue();
//            return new LengthOperand((PropertyValueOperand) propValue.accept(this, null));
//        }
//
//        public Object visit(LiteralImpl node, Object data) throws Exception {
//            return node.getLiteralValue();
//        }
//
//        public Object visit(LowerCaseImpl node, Object data) throws Exception {
//            DynamicOperandImpl operand = (DynamicOperandImpl) node.getOperand();
//            return new LowerCaseOperand((DynamicOperand) operand.accept(this, data));
//        }
//
//        public Object visit(NodeLocalNameImpl node, Object data) throws Exception {
//            return new NodeLocalNameOperand();
//        }
//
//        public Object visit(NodeNameImpl node, Object data) throws Exception {
//            Value staticValue = (Value) data;
//            switch (staticValue.getType()) {
//                // STRING, PATH and URI may be convertable to a NAME -> check
//                case PropertyType.STRING:
//                case PropertyType.PATH:
//                case PropertyType.URI:
//                    // make sure static value is valid NAME
//                    try {
//                        String s = staticValue.getString();
//                        if (staticValue.getType() == PropertyType.URI) {
//                            if (s.startsWith("./")) {
//                                s = s.substring(2);
//                            }
//                            // need to decode
//                            s = URLDecoder.decode(s, "UTF-8");
//                        }
//                        vf.createValue(s, PropertyType.NAME);
//                    } catch (ValueFormatException e) {
//                            throw new InvalidQueryException("Value " +
//                                staticValue.getString() +
//                                " cannot be converted into NAME");
//                    }
//                    break;
//                // the following types cannot be converted to NAME
//                case PropertyType.DATE:
//                case PropertyType.DOUBLE:
//                case PropertyType.DECIMAL:
//                case PropertyType.LONG:
//                case PropertyType.BOOLEAN:
//                case PropertyType.REFERENCE:
//                case PropertyType.WEAKREFERENCE:
//                    throw new InvalidQueryException(staticValue.getString() +
//                            " cannot be converted into a NAME value");
//            }
//
//            return new NodeNameOperand();
//        }
//
//        public Object visit(NotImpl node, Object data) throws Exception {
//            ConstraintImpl c = (ConstraintImpl) node.getConstraint();
//            return new NotConstraint((Constraint) c.accept(this, null));
//        }
//
//        public Object visit(OrImpl node, Object data) throws Exception {
//            ConstraintImpl left = (ConstraintImpl) node.getConstraint1();
//            ConstraintImpl right = (ConstraintImpl) node.getConstraint2();
//            return new OrConstraint((Constraint) left.accept(this, null),
//                    (Constraint) right.accept(this, null));
//        }
//
//        public Object visit(PropertyExistenceImpl node, Object data)
//                throws Exception {
//            return new PropertyExistenceConstraint(node,
//                    getSelector(node.getSelectorQName()), factory);
//        }
//
//        public Object visit(PropertyValueImpl node, Object data) throws Exception {
//            return new PropertyValueOperand(node);
//        }
//
//        public Object visit(SameNodeImpl node, Object data) throws Exception {
//            return new SameNodeConstraint(node,
//                    getSelector(node.getSelectorQName()));
//        }
//
//        public Object visit(UpperCaseImpl node, Object data) throws Exception {
//            DynamicOperandImpl operand = (DynamicOperandImpl) node.getOperand();
//            return new UpperCaseOperand((DynamicOperand) operand.accept(this, data));
//        }
//
//        private SelectorImpl getSelector(Name name) {
//            for (SelectorImpl selector : selectors) {
//                if (selector.getSelectorQName().equals(name)) {
//                    return selector;
//                }
//            }
//            return null;
//        }
//    }
//
//}
