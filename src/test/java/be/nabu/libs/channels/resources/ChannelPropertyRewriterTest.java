/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.channels.resources;

import java.util.List;

public class ChannelPropertyRewriterTest {
	
	public static void testRewrite() {
		ChannelPropertyRewriter rewriter = new ChannelPropertyRewriter();
		TestProperties properties = new TestProperties();
		properties.setTestValue("test/${format(now() - '2m' + '1d', 'yyyy/MM/dd')}/dir");
		List<TestProperties> rewrite = rewriter.rewrite(properties);
		System.out.println(rewrite);
	}
	
	public static void testListRewrite() {
		ChannelPropertyRewriter rewriter = new ChannelPropertyRewriter();
		TestProperties properties = new TestProperties();
		properties.setTestValue("test/${range(now() - '2y', now(), '1y', 'yyyy')}/${range(now() - '2d', now(), '1d', 'MM/dd')}/dir");
		properties.setSecondTestValue("fixed");
		properties.setThirdTestValue("test/${format(now(), 'yyyy/MM/dd')}");
		List<TestProperties> rewrite = rewriter.rewrite(properties);
		System.out.println(rewrite);
	}
	
	public static class TestProperties {
		private String testValue, secondTestValue, thirdTestValue;

		public String getTestValue() {
			return testValue;
		}

		public void setTestValue(String testValue) {
			this.testValue = testValue;
		}
		
		@Override
		public String toString() {
			return testValue;
		}

		public String getSecondTestValue() {
			return secondTestValue;
		}

		public void setSecondTestValue(String secondTestValue) {
			this.secondTestValue = secondTestValue;
		}

		public String getThirdTestValue() {
			return thirdTestValue;
		}

		public void setThirdTestValue(String thirdTestValue) {
			this.thirdTestValue = thirdTestValue;
		}
	}
}
