/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.core.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 *  [ 通用树状结构构造器 ]
 *  解决： 将数据库查询到的多行List， 转换为层级关系的树状结构。
 *  使用方式：
 *      1. 先将查询的到对象List转换为JSONObject List，
 *         在转换过程中JSONObject中必须包含 [id, pid](字段名称可自定义) 【！！必须是String类型！！】 ；
 *      2. 使用构造函数创建对象，参数为转换好的对象， 如果自定义字段key 则将字段名称一并传入；
 *      3. 使用buildTreeString() 或者 buildTreeObject() 生成所需对象；
 *
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2019/12/8 06:37
*/
public class TreeDataBuilder {


    /** 私有构造器 + 指定参数构造器 **/
    private TreeDataBuilder(){}
    public TreeDataBuilder(Collection nodes) {
        super();
        this.nodes = nodes;
    }

    public TreeDataBuilder(Collection nodes, String idName, String pidName, String childrenName) {
        super();
        this.nodes = nodes;
        this.idName = idName;
        this.sortName = idName;  //排序字段，按照idName
        this.pidName = pidName;
        this.childrenName = childrenName;
    }

    /** 自定义字段 + 排序标志 **/
    public TreeDataBuilder(Collection nodes, String idName, String pidName, String childrenName, String sortName, boolean isAscSort) {
        super();
        this.nodes = nodes;
        this.idName = idName;
        this.pidName = pidName;
        this.childrenName = childrenName;
        this.sortName = sortName;
        this.isAscSort = isAscSort;
    }

    /** 所有数据集合 **/
    private Collection<JSONObject> nodes;

    /** 默认数据中的主键key */
    private String idName = "id";

    /** 默认数据中的父级id的key */
    private String pidName = "pid";

    /** 默认数据中的子类对象key   */
    private String childrenName = "children";

    /** 排序字段， 默认按照ID排序 **/
    private String sortName = idName;

    /** 默认按照升序排序 **/
    private boolean isAscSort = true;

    // 构建JSON树形结构
    public String buildTreeString() {
        List<JSONObject> nodeTree = buildTreeObject();
        JSONArray jsonArray = new JSONArray();
        nodeTree.stream().forEach(item -> jsonArray.add(item));
        return jsonArray.toString();
    }

    // 构建树形结构
    public List<JSONObject> buildTreeObject() {

        //定义待返回的对象
        List<JSONObject> resultNodes = new ArrayList<>();

        //获取所有的根节点 （考虑根节点有多个的情况， 将根节点单独处理）
        List<JSONObject> rootNodes = getRootNodes();

        listSort(rootNodes); //排序

        //遍历根节点对象
        for (JSONObject rootNode : rootNodes) {

            buildChildNodes(rootNode); //递归查找子节点并设置

            resultNodes.add(rootNode); //添加到对象信息
        }
        return resultNodes;
    }

    /** 递归查找并赋值子节点 **/
    private void buildChildNodes(JSONObject node) {
        List<JSONObject> children = getChildNodes(node);
        if (!children.isEmpty()) {
            for (JSONObject child : children) {
                buildChildNodes(child);
            }

            listSort(children); //排序
            node.put(childrenName, children);
        }
    }

    /** 查找当前节点的子节点 */
    private List<JSONObject> getChildNodes(JSONObject currentNode) {
        List<JSONObject> childNodes = new ArrayList<>();
        for (JSONObject n : nodes) {
            if (currentNode.getString(idName).equals(n.getString(pidName))) {
                childNodes.add(n);
            }
        }
        return childNodes;
    }

    /** 判断是否为根节点 */
    private boolean isRootNode(JSONObject node) {
        boolean isRootNode = true;
        for (JSONObject n : nodes) {
            if (node.getString(pidName) != null && node.getString(pidName).equals(n.getString(idName))) {
                isRootNode = false;
                break;
            }
        }
        return isRootNode;
    }

    /** 获取集合中所有的根节点 */
    private List<JSONObject> getRootNodes() {
        List<JSONObject> rootNodes = new ArrayList<>();
        for (JSONObject n : nodes) {
            if (isRootNode(n)) {
                rootNodes.add(n);
            }
        }
        return rootNodes;
    }

    /** 将list进行排序  */
    private void listSort(List<JSONObject> list){
        Collections.sort(list, (o1, o2) -> {

            int result;
            if(o1.get(sortName) instanceof Integer){
                result = o1.getInteger(sortName).compareTo(o2.getInteger(sortName));
            }else{
                result = o1.get(sortName).toString().compareTo(o2.get(sortName).toString());
            }

            if(!isAscSort){  //倒序， 取反数
                return -result;
            }

            return result;
        });
    }

}
