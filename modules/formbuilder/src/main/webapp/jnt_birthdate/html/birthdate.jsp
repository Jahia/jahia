<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<label class="left">${fn:escapeXml(currentNode.properties.label.string)}</label>
<span>
          <select name="year">
              <option><fmt:message key="label.year"/></option>
              <option>1950</option>
              <option>1951</option>
              <option>1952</option>
              <option>1953</option>
              <option>1954</option>
              <option>1955</option>
              <option>1956</option>
              <option>1957</option>
              <option>1958</option>
              <option>1959</option>
              <option>1960</option>
              <option>1961</option>
              <option>1962</option>
              <option>1963</option>
              <option>1964</option>
              <option>1965</option>
              <option>1966</option>
              <option>1967</option>
              <option>1968</option>
              <option>1969</option>
              <option>1970</option>
              <option>1971</option>
          </select>
    <select name="month">
        <option><fmt:message key="label.month"/></option>
              <option>1</option>
              <option>2</option>
              <option>3</option>
              <option>4</option>
              <option>5</option>
              <option>6</option>
              <option>7</option>
              <option>8</option>
              <option>9</option>
              <option>10</option>
              <option>11</option>
              <option>12</option>
          </select>
    <select name="day">
        <option><fmt:message key="label.day"/></option>
              <option>1</option>
              <option>2</option>
              <option>3</option>
              <option>4</option>
              <option>5</option>
              <option>6</option>
              <option>7</option>
              <option>8</option>
              <option>9</option>
              <option>10</option>
              <option>11</option>
              <option>12</option>
              <option>13</option>
              <option>14</option>
              <option>15</option>
              <option>16</option>
              <option>17</option>
              <option>18</option>
              <option>19</option>
              <option>20</option>
              <option>21</option>
              <option>22</option>
              <option>23</option>
              <option>24</option>
              <option>25</option>
              <option>26</option>
              <option>27</option>
              <option>28</option>
              <option>29</option>
              <option>30</option>
              <option>31</option>
          </select>
</span>